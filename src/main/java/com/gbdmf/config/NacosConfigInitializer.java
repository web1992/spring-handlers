//package com.gbdmf.config;
//
//import com.alibaba.nacos.api.NacosFactory;
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.config.ConfigType;
//import com.alibaba.nacos.api.config.listener.Listener;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.spring.context.annotation.config.NacosValueAnnotationBeanPostProcessor;
//import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
//import com.internet.configcenter.util.NacosContetxUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContextInitializer;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.env.MapPropertySource;
//import org.springframework.core.env.MutablePropertySources;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.Executor;
//
//import static com.internet.configcenter.constans.Constans.LOG_PREFIX;
//
//public class NacosConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
//
//    private static final Logger log = LoggerFactory.getLogger(NacosConfigInitializer.class);
//
//    private NacosValueAnnotationBeanPostProcessor nacosValueAnnotationBeanPostProcessor;
//    private ConfigService configService;
//    private static final String GROUP = "DEFAULT_GROUP";
//    public static String curDataId = null;
//
//    @Override
//    public void initialize(ConfigurableApplicationContext applicationContext) {
//        try {
//            // 1. 初始化 Nacos ConfigService
//            initNacosConfigService();
//
//            // 2. 首次加载配置
//            loadAndMergeConfig(applicationContext);
//
//            // 3. 添加监听器实现动态刷新
//            addConfigListener(applicationContext);
//
//            // 注册一个监听器，在容器刷新完成后获取 Bean
//            applicationContext.addApplicationListener(new ApplicationListener<ContextRefreshedEvent>() {
//                @Override
//                public void onApplicationEvent(ContextRefreshedEvent event) {
//                    // 检查Bean是否存在
//                    // ApplicationContext 存在多个，并且存在父子关系，所以需要判断是否存在这个 bean
//                    if (event.getApplicationContext().containsBeanDefinition(NacosValueAnnotationBeanPostProcessor.BEAN_NAME)) {
//                        try {
//                            if (null != NacosConfigInitializer.this.nacosValueAnnotationBeanPostProcessor) {
//                                log.info("{} NacosValueAnnotationBeanPostProcessor is already registered in application context", LOG_PREFIX);
//                                return;
//                            }
//                            NacosConfigInitializer.this.nacosValueAnnotationBeanPostProcessor = event.getApplicationContext().getBean(NacosValueAnnotationBeanPostProcessor.class);
//                        } catch (BeansException e) {
//                            log.error("Failed to get NacosValueAnnotationBeanPostProcessor", e);
//                        }
//                    } else {
//                        log.warn("NacosValueAnnotationBeanPostProcessor is not registered in application context");
//                    }
//                }
//            });
//        } catch (NacosException e) {
//            throw new RuntimeException("Failed to initialize Nacos configuration", e);
//        }
//    }
//
//    private void initNacosConfigService() throws NacosException {
//        curDataId = NacosContetxUtils.getCurDataId();
//        String serverAddr = NacosContetxUtils.getServerAddr();
//        String nameSpace = NacosContetxUtils.getNameSpace();
//
//        log.info("{} NacosConfigPropsInitializer initialize curDataId {}", LOG_PREFIX, curDataId);
//        log.info("{} NacosConfigPropsInitializer initialize serverAddr {}", LOG_PREFIX, serverAddr);
//        log.info("{} NacosConfigPropsInitializer initialize nameSpace {}", LOG_PREFIX, nameSpace);
//
//        Properties properties = new Properties();
//        properties.put("serverAddr", serverAddr); // Nacos 服务器地址
//        properties.put("namespace", nameSpace);  // 可选命名空间
//        configService = NacosFactory.createConfigService(properties);
//    }
//
//    private void loadAndMergeConfig(ConfigurableApplicationContext context) throws NacosException {
//        // 获取配置内容
//        String configContent = configService.getConfig(curDataId, GROUP, 5000);
//
//        // 解析配置内容（这里以简单 key=value 为例）
//        Map<String, Object> configMap = parseConfigContent(configContent);
//
//        // 将配置合并到 Spring Environment
//        ConfigurableEnvironment environment = context.getEnvironment();
//        MutablePropertySources propertySources = environment.getPropertySources();
//
//        // 移除旧的配置源（如果存在）
//        if (propertySources.contains("nacosConfig")) {
//            propertySources.remove("nacosConfig");
//        }
//        // 添加新的配置源（优先级最高）
//        propertySources.addFirst(new MapPropertySource("nacosConfig", configMap));
//    }
//
//    private Map<String, Object> parseConfigContent(String content) {
//        // 简单解析 key=value 格式
//        Map<String, Object> configMap = new HashMap<>();
//        for (String line : content.split("\n")) {
//            String[] parts = line.split("=", 2);
//            if (parts.length == 2) {
//                configMap.put(parts[0].trim(), parts[1].trim());
//            }
//        }
//        return configMap;
//    }
//
//    private void addConfigListener(ConfigurableApplicationContext context) throws NacosException {
//        log.info("{} addConfigListener curDataId {}", LOG_PREFIX, curDataId);
//        configService.addListener(curDataId, GROUP, new Listener() {
//            @Override
//            public Executor getExecutor() {
//                return null; // 使用默认执行器
//            }
//
//            @Override
//            public void receiveConfigInfo(String configInfo) {
//                // 配置变更时重新加载
//                log.info("Nacos config changed, reloading...");
//                try {
//                    loadAndMergeConfig(context);
//                    if (null != nacosValueAnnotationBeanPostProcessor) {
//                        nacosValueAnnotationBeanPostProcessor.setEnvironment(context.getEnvironment());
//                        // 让 支持 NacosValue 注解 支持自动刷新
//                        nacosValueAnnotationBeanPostProcessor.onApplicationEvent(new NacosConfigReceivedEvent(configService, curDataId, GROUP, configInfo, ConfigType.PROPERTIES.getType()));
//                    }
//                } catch (Exception e) {
//                    log.error("Failed to reload Nacos configuration", e);
//                }
//                // 如果需要可以刷新上下文
//                // context.refresh();
//            }
//        });
//    }
//}