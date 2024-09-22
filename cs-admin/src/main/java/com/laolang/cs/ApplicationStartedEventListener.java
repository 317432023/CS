package com.laolang.cs;

import cn.hutool.core.net.NetUtil;
import org.jboss.logging.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

class ApplicationStartedEventListener implements GenericApplicationListener {
    public static final int DEFAULT_ORDER = -2147483638;
    private static Class<?>[] EVENT_TYPES = new Class[]{ApplicationStartingEvent.class, ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ContextClosedEvent.class, ApplicationFailedEvent.class};
    private static Class<?>[] SOURCE_TYPES = new Class[]{SpringApplication.class, ApplicationContext.class};

    ApplicationStartedEventListener() {
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ConfigurableEnvironment envi = ((ApplicationEnvironmentPreparedEvent)event).getEnvironment();
            MutablePropertySources mps = envi.getPropertySources();
            PropertySource<?> ps = mps.get("configurationProperties");
            if (ps != null) {
                String ip;
                if (ps.containsProperty("spring.application.name")) {
                    ip = (String)ps.getProperty("spring.application.name");
                    MDC.put("appName", ip);
                }

                ip = NetUtil.getLocalhost().getHostAddress();
                if (!StringUtils.isEmpty(ip)) {
                    MDC.put("ip", ip);
                }

                if (ps.containsProperty("server.port")) {
                    Object port = ps.getProperty("server.port");
                    MDC.put("port", port.toString());
                }
            }
        }

    }

    public int getOrder() {
        return -2147483638;
    }

    public boolean supportsEventType(ResolvableType resolvableType) {
        return this.isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    public boolean supportsSourceType(Class<?> sourceType) {
        return this.isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            Class[] var3 = supportedTypes;
            int var4 = supportedTypes.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Class<?> supportedType = var3[var5];
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }

        return false;
    }
}
