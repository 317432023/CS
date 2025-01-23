package com.laolang.cs;

import com.soaringloong.jfrm.framework.mybatis.core.mapper.SpringBeanNameGenerator;
import com.soaringloong.jfrm.framework.web.core.listener.ApplicationStartedEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

@SpringBootApplication(nameGenerator = SpringBeanNameGenerator.class)
//@ServletComponentScan("com.tk.ind.web.body") using CacheRequestBodyFilter instead.
public class CsApplication {
    public static void main(String[] args) {
        final Class<?> clazz = CsApplication.class;
        String pidfile = args != null && args.length > 0 ? args[0] : null;
        SpringApplicationBuilder builder = (new SpringApplicationBuilder(new Class[]{clazz})).initializers(new ApplicationContextInitializer[]{(context) -> {
        }});
        SpringApplication app = builder.build();
        if (StringUtils.hasLength(pidfile)) {
            app.addListeners(new ApplicationListener[]{new ApplicationPidFileWriter(pidfile)});
        }

        app.addListeners(new ApplicationListener[]{new ApplicationStartedEventListener()});
        app.run(args);
    }

}
