# 本文件为 application.yml 的配置说明备注文件

server:
  port: 8080
spring:
  main:
    lazy-initialization: false # 延时加载 springboot 2.2+ 特性
    banner-mode: off
  application:
    name: TPL-ONE
  profiles:
    active: dev # 环境 debug|dev|test|prod
  output:
    ansi:
      enabled: always

  transaction:
    rollback-on-commit-failure: true  # true 表示开启事务失败回滚(默认值是false)

  mvc:
    throw-exception-if-no-handler-found: true #出现错误时, 直接抛出异常
    view:
      suffix: .html
    # 静态资源访问路径 模式匹配后按 spring.resources.static-locations 指定查找的本地文件的位置
    # static-path-pattern: /static/** # 转在 WebConfig.java 中做代码级的处理 见 WebConfig.java#addResourceHandlers

    # 自定义属性
    cors-filter: true # 是否实例化(开启)跨域过滤器

  resources:
    # 静态资源默认存放位置
    #static-locations: classpath:/META-INF/resources/, classpath:/resources/, classpath:/static/, classpath:/public/
    # ！！！如果工程仅仅作为服务接口可以关闭映射(设置为false)
    add-mappings: false # 不要为我们工程中的资源文件建立映射(默认true)

  jackson:
    #date-format: yyyy-MM-dd HH:mm:ss # 转在 WebConfig.java 中做代码级的处理
    #time-zone: Asia/Shanghai # 转在 WebConfig.java 中做代码级的处理
    #time-zone: GMT+8 # 转在 WebConfig.java 中做代码级的处理
    serialization:
      write-dates-as-timestamps: false
    #generator:
    #  write_numbers_as_strings: true # true 表示将数值类型转换为字符串，解决long型精度丢失(默认值是false)，其缺点时粒度太大，所有数字都被转成字符串输出了，包括日期戳类型。# 转在 WebConfig.java 中做代码级的处理

  # 缓存策略 整合 SpringCache
  cache: # 依赖spring-boot-starter-cache；由 @EnableCaching 开启，支持@CachePut | @CacheEvict | @Cacheable
    type: redis
    redis:
      time-to-live: 3600s

  # 会话存储策略 整合 SpringSession
  session: # 依赖spring-session-core；由 @EnableRedisHttpSession 开启
    store-type: redis # redis 或 jdbc 或 none
    timeout: 3600s
    redis: # 依赖spring-session-data-redis 与 spring-boot-starter-data-redis
      cleanup-cron: 0 */30 * * * ? # 半小时清理session,cron表达式可以自行百度
      namespace: spring:session
      flush-mode: on_save

  #  jdbc: # 依赖spring-session-data-jdbc
  #    cleanup-cron: 0 */30 * * * ?  # 半小时清理session,cron表达式可以自行百度
  #    table-name: SPRING_SESSION
  #    initialize-schema: embedded
  #    schema: classpath:schema-sqlserver.sql # 这个sql文件来自spring-session-jdbc*.jar

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true # 将数据库的带下划线“”给去掉然后映射到实体类的属性上去
  global-config:
    banner: false
    db-config:
      logic-delete-field: deleted # 全局逻辑删除的实体字段名（自3.3.0，配置后可以忽略不配置实体类字段标注@TableLogic private Integer deleted;）
      logic-delete-value: 1 # 逻辑已删除值（默认 1）
      logic-not-delete-value: 0 # 逻辑未删除值（默认为 0）
  # xml扫描，一般这样配置如classpath*:**/mapper/**/*Mapper.xml 多个目录可以用逗号或者分号分隔（告诉 Mapper 所对应的 XML 文件位置）
  # 若和mapper接口放在同一包下，就不需要特殊指定
  #mapper-locations: classpath*:**/mapper/*Mapper.xml
  #type-aliases-package: com.soarer.seed.sys.model.entity


####################################
# 分页插件配置
####################################
pagehelper:
  helperDialect: mysql
  # 设置为ture时，当pageNum<=0时会查询第一页；pageNum>=pages会查询最后一页
  reasonable: true
  params: count=countSql
  # 支持通过 Mapper 接口参数来传递分页参数，默认值false，分页插件会从查询方法的参数值中，自动根据上面 params 配置的字段中取值，查找到合适的值时就会自动分页
  support-methods-arguments: true

# 自定义属性文件
# 见 com.cmpt.sys.security.config.SettingProperties
# 见 com.cmpt.captcha.config.CaptchaProperties
# 见 com.cmpt.oss.upload.config.OssProperties
config:
  setting: ./TPL-ONE/src/main/resources/setting.properties # 这种格式仅在Idea下运行时才能找到正确目录
  #setting: ./src/main/resources/setting.properties # 这种格式仅在Idea下运行测试案例时才能找到正确目录
  captcha: ./TPL-ONE/src/main/resources/captcha.properties # 这种格式仅在Idea下运行时才能找到正确目录
  oss: ./TPL-ONE/src/main/resources/oss.properties # 这种格式仅在Idea下运行时才能找到正确目录
