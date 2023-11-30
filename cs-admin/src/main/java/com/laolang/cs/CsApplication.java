package com.laolang.cs;

import com.frm.springboot.SpringBootMain;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @ClassName CsApplication
 * @Description Customer Service
 * @Date 2020/11/12 23:11
 * @Version V1.0
 */
@SpringBootApplication
@ServletComponentScan("com.tk.ind.web.body")
@org.mybatis.spring.annotation.MapperScan({"com.cmpt.**.mapper","com.laolang.cs"})
public class CsApplication {
    public static void main(String[] args) {
        final Class<?> clazz = CsApplication.class;
        SpringBootMain.startup(clazz, args);
    }

}
