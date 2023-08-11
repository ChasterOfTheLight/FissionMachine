# FissionMachine
A machine that meets the requirements of project fission and is suitable for quickly building a Spring Cloud scaffolding project.Using these tools and technologies, you can quickly set up a Spring Cloud project and meet the requirements of project fission, allowing for efficient development and scalability.

## Technology Stack

- JDK1.8
- Spring Boot2.3
- Spring Cloud Hoxton
- Nacos2.X
- Mybatis Plus3.5.3.1
- Redisson3.16.7
- Jetcache2.6.5
- Hutool5.8.12
- JWT
- RabbitMQ
- Mysql
- Redis

## Architecture

* **Project Directory**

```text
FissionMachine （项目根目录）
  ├─common（公共工具和方法）
  ├─framework（免配置工具）
  ├─infrastructure（基建工程）
  ├─service-common（启动服务公共工具和方法）
  ├─service-modules（服务工程）
```

* **Base Process**

```text
  +---------+
  | request |
  +---------+
       ^
       |
       v
  +---------+          +--------------+
  | gateway |  < —— >  | auth-service |
  +---------+          +--------------+
       ^
       |
       v
  +-----------------+
  | example-service |
  +-----------------+
```

## Common Config

* nacos公共配置common-config.yaml
```yaml
server:
  compression:
    enabled: true
    mime-types:
      - text/xml
      - application/xml
      - application/json
    min-response-size: 2048
# feign
feign:
  # user okhttp client pool
  okhttp:
    enabled: true
  sentinel:
    enabled: true
  client:
    config:
      default:
        # 连接超时时间
        connectTimeout: 5000
        readTimeout: 5000
  compression:
    request:
      enabled: true
      mime-types:
        - text/xml
        - application/xml
        - application/json
      min-request-size: 2048
    response:
      enabled: true
      useGzipDecoder: true
# management
management:
  endpoint:
    health:
      cache:
        time-to-live: 5000
      enabled: true
# mybatis-plus
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    # 开启打印sql，不配置不打印
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## Gateway Config

* nacos config

```yaml
# routes config
spring:
  cloud:
    gateway:
      routes:
        - id: example
          predicates:
            - Path=/example/**
          uri: lb://example-service
          filters:
            # 是否对此路由打印接口请求时间（对应RequestTimeGatewayFilterFactory类的实现），true代表打印get参数
            - RequestTime=true
            # swagger地址特殊处理，数字代表地址跳过几个/，如1代表：/example/v2/api-docs -> /v2/api-docs
            - SwaggerPathStripPrefix=3

# white or black List config
gateway-config:
  ipBlackList:
    # ip黑名单配置，配置在此黑名单，无法访问接口
    - 192.168.1.1
  uriWhitelist:
    # uri白名单，配置在此列表的接口不需要走认证逻辑，支持ant类型的uri
    - /**/v2/api-docs
```

* 接口文档访问gateway地址的/doc.html
* 接口文档枚举在gateway枚举类ServerRouteEnum配置

## Version Management

```shell
# change version
mvn versions:set -DnewVersion=1.0.0
```

```shell
# revert
mvn versions:revert
```

## Swagger Use

```yaml
swagger:
  enable: true
  application-name: XXX服务接口
  application-version: 2.0
  application-description: XXX服务接口 
```

## Xss Custom Config

```java
@Component
public class ExampleConfig {
    
    @Bean
    public FilterRegistrationBean<Filter> xssFilterRegistration(XssFilter filter) {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        filterRegistrationBean.addUrlPatterns("/*");
        Map<String, String> initParameters = new HashMap<>();
        // apis not handle xss  一些富文本的场景需要加入到这个排除
        initParameters.put("exclusions", "/api1,/api2");
        filterRegistrationBean.setInitParameters(initParameters);
        filterRegistrationBean.setOrder(6);
        return filterRegistrationBean;
    }
}
```

## Auth OpenApi Config

```yaml
machine:
  auth:
    access:
    # 数组，可配置多个
    - accessKey: your_accessKey
      accessSecret: your_accessSecret
      # 访问来源
      accessSource: business_name
      # 可选 不配置默认可以访问全部uri
      accessUriList:
        - /example
```

- **sign generate rule**

```java
class SignGen {
    
    public String genSign() {
        // 对参数排序
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("accessKey", "your_accessKey");
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        treeMap.put("nonce", "random_or_custom_str");
        treeMap.put("timestamp", currentTimeMillis);
        String argStr = treeMap.entrySet().stream().map(Object::toString).collect(Collectors.joining("&"));
        String accessSecret = "your_accessSecret";
        String secretStr = argStr + "&accessSecret=" + accessSecret;
        return DigestUtils.md5DigestAsHex(secretStr.getBytes(StandardCharsets.UTF_8)).toUpperCase(Locale.ROOT);
        // 将生成的sign，accessKey，nonce，timestamp放到请求头,请求头分别为：
        // x-auth-access-key
        // x-auth-timestamp
        // x-auth-nonce
        // x-auth-sign
    }
    
}
```

## Jdbc Config

```yaml
spring:
  datasource: 
    hikari:
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://localhost:3306/example?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true
      username: root
      password: root
```

## Multi DataSource Config

```yaml
spring:
  datasource:
    dynamic:
      primary: master #设置默认的数据源或者数据源组,默认值即为master
      strict: false #严格匹配数据源,默认false. true未匹配到指定数据源时抛异常,false使用默认数据源
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/sample?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave_1:
          url: jdbc:mysql://localhost:3306/sample_slave1?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&allowPublicKeyRetrieval=true
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver
```

## Redis Config

```yaml
spring:
  redis:
    database: 1
    host: localhost
    port: 6379
    password: your_redis_password
    lettuce:
      pool:
        max-active: 1000
        max-idle: 10
        max-wait: -1
        min-idle: 5
    timeout: 6000
```