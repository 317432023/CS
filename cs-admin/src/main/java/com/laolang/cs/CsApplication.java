package com.laolang.cs;

import com.frm.springboot.SpringBootMain;
import com.soaringloong.jfrm.framework.mybatis.core.mapper.SpringBeanNameGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(nameGenerator = SpringBeanNameGenerator.class)
@ServletComponentScan("com.tk.ind.web.body")
public class CsApplication {
    public static void main(String[] args) {
        final Class<?> clazz = CsApplication.class;
        SpringBootMain.startup(clazz, args);
    }

}
