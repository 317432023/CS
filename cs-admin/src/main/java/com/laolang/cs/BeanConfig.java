package com.laolang.cs;

import com.cmpt.ws.SocketChannelInterceptor;
import com.cmpt.ws.WebSocketNativeConfig;
import com.cmpt.ws.WebSocketStompConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * <p>扫描存放在主类包外部的一切Spring Bean</p>
 * 包含@Service、@Component、@Aspect、@Repository、@Configuration等spring标注
 */
@Configuration
@ComponentScan(basePackages = {
    // 基础接口
    "com.comm.infra",
    /*框架*/
    "com.frm.springmvc",
    "com.frm.springboot",
    "com.frm.redis",
    "com.frm.mybatisplus",
    /*组件:文档*/"com.cmpt.doc",
    /*组件:验证码*/"com.cmpt.captcha",
    /*组件:文件上传*/"com.cmpt.oss",
    /*组件:Websocket*/"com.cmpt.ws",
    /*组件:多租戶*/"com.cmpt.org",
    /*控制台API*/"com.cmpt.sys",
    /*会员API规格*/"com.cmpt.mbr",
    /*社交用户API*/"com.cmpt.sns",

}, /*不纳入扫描的类*/excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebSocketNativeConfig.class, WebSocketStompConfig.class, SocketChannelInterceptor.class})})
public class BeanConfig {


}
