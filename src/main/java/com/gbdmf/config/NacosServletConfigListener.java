package com.gbdmf.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.internet.configcenter.constans.ConfigConstans;
import com.internet.configcenter.props.RemotePropertyLoader;
import com.internet.configcenter.util.ConfigUtil;
import com.internet.configcenter.util.EnvUtil;
import com.internet.configcenter.util.NacosContetxUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.internet.configcenter.constans.Constans.LOG_PREFIX;
import static com.internet.configcenter.constans.Constans.NACOS_CONFIG_DATA_IDS;

/**
 * @author: ZG
 * @since: 2025/6/10
 * @version: 1.0
 */
@WebListener
public class NacosServletConfigListener implements ServletContextListener {


    private static final Logger log = LoggerFactory.getLogger(NacosServletConfigListener.class);

    private ConfigService configService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("{} start preload environment in web listener.", LOG_PREFIX);
        ServletContext servletContext = sce.getServletContext();
        String dataId = servletContext.getInitParameter(ConfigConstans.RUNTIME_SYSTEM_ALIAS);
        String useLocalFile = servletContext.getInitParameter(ConfigConstans.USE_LOCAL_FILE);
        log.info("{} dataId is {}", LOG_PREFIX, dataId);
        log.info("{} useLocalFile is {}", LOG_PREFIX, useLocalFile);
        if (StringUtils.isBlank(dataId)) {
            log.warn("{} dataId is not set in application.properties with key runtime.systemAlias.", LOG_PREFIX);
            return;
        }

        Properties content = getPropertiesContent(dataId);
        if (content == null) {
            return;
        }

        String specifyEnv = EnvUtil.getServerEnvNew();
        dataId = dataId + "-" + (specifyEnv);
        String curDataId = dataId;
        Map<String, Object> map = new HashMap<>();
        map.put(ConfigConstans.CONFIG_DATA_ID_WITH_ENV, curDataId);
        content.forEach((key, value) -> {
            map.put(key.toString(), value);
        });
        NacosContetxUtils.set(map);
        // 如果启用了这个配置，那么不需要使用 NacosConfigInitializer 进行属性注入了
        // initNacosConfigService 方法会下载 props配置 ，创建文件到 class 下面
        // 通过代码引用 classpath:*.properties 即可
        // 或者使用下面的配置
        //   <context:property-placeholder location="WEB-INF/classes/*.properties" file-encoding="utf-8" ignore-unresolvable="true"/>
        if (ConfigConstans.USE_LOCAL_FILE_FLAG.equals(useLocalFile)) {
            String dataIdString = buildDataIdString(curDataId, content);
            initNacosConfigService(content, dataIdString);
        }
    }

    private Properties getPropertiesContent(String dataId) {
        String specifyEnv = EnvUtil.getServerEnvNew();
        log.info("{} serverEnv is {}", LOG_PREFIX, specifyEnv);

        StringBuilder providerVersion = new StringBuilder(ConfigConstans.PUBLIC_PROVIDER_VERSION);
        StringBuilder referenceVersion = new StringBuilder(ConfigConstans.PUBLIC_REFERENCE_VERSION);
        StringBuilder dataIds = new StringBuilder();

        return loadProperties(specifyEnv, dataId, dataIds, providerVersion, referenceVersion);
    }

    private Properties loadProperties(String specifyEnv, String dataId, StringBuilder dataIds,
                                      StringBuilder providerVersion, StringBuilder referenceVersion) {
        Properties content;
        if (StringUtils.isBlank(specifyEnv)) {
            log.warn("{} The server is not configured for the environment,Please check", LOG_PREFIX);
            if (StringUtils.isBlank(specifyEnv)) {
                log.warn("{} environment not currently acquired", LOG_PREFIX);
                return null;
            }
            content = LocalPropertyLoader.loadApplicationProperties(specifyEnv);
            if (!ConfigConstans.LOCAL.equals(specifyEnv)) {
                dataIds.append(dataId).append("-").append(specifyEnv);
            }
        } else {
            content = LocalPropertyLoader.loadApplicationProperties(specifyEnv);
            dataIds.append(dataId).append("-").append(specifyEnv);
            handleServerEnvRegion(specifyEnv, dataIds, providerVersion, referenceVersion);
        }
        return content;
    }

    private void handleServerEnvRegion(String specifyEnv, StringBuilder dataIds,
                                       StringBuilder providerVersion, StringBuilder referenceVersion) {
        String serverEnvRegion = EnvUtil.getServerEnvRegion();
        if (StringUtils.isNotBlank(serverEnvRegion) && !StringUtils.equals("prod", specifyEnv)) {
            if (!StringUtils.equals("0", serverEnvRegion)) {
                dataIds.append(serverEnvRegion);
                providerVersion.append(serverEnvRegion);
                referenceVersion.append(serverEnvRegion);
            }
        }
    }

    private String buildDataIdString(String dataIdx, Properties content) {
        StringBuilder dataIds = new StringBuilder();
        dataIds.append(dataIdx);
        String dataIdString = dataIds.toString();
        if (StringUtils.isNotBlank(dataIdString)) {
            StringBuilder providerVersion = new StringBuilder(ConfigConstans.PUBLIC_PROVIDER_VERSION);
            StringBuilder referenceVersion = new StringBuilder(ConfigConstans.PUBLIC_REFERENCE_VERSION);
            dataIdString = ConfigUtil.appendDataId(dataIdString, ConfigConstans.PUBLIC_CONFIG_DATA_ID,
                    providerVersion.toString(), referenceVersion.toString());
            content.setProperty(NACOS_CONFIG_DATA_IDS, dataIdString);
            EnvUtil.setDataIdString(dataIds.toString());
        }
        return dataIdString;
    }

    private void initNacosConfigService(Properties content, String dataIdString) {
        PropertySource<?> propertySource = new PropertiesPropertySource("custom-resource", content);
        Object nacosServerAddr = propertySource.getProperty("nacos.config.server-addr");
        Object nacosNameSpace = propertySource.getProperty("nacos.config.namespace");

        try {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosServerAddr);
            properties.put("namespace", nacosNameSpace);
            configService = NacosFactory.createConfigService(properties);

            for (String nacosDataId : dataIdString.split(",")) {
                String config = configService.getConfig(nacosDataId, "DEFAULT_GROUP", 5000);
                log.info("Initial config from Nacos: " + config);
                RemotePropertyLoader.load(nacosDataId, config);
            }
        } catch (Exception e) {
            log.error("Error initializing Nacos", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 可添加清理逻辑
    }
}