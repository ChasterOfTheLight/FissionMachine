# FissionMachine

A machine that meets the requirements of project fission and is suitable for quickly building a Spring Cloud scaffolding project.Using these tools and
technologies, you can quickly set up a Spring Cloud project and meet the requirements of project fission, allowing for efficient development and
scalability.

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
  # use okhttp client pool
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

- Import Dependency

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-jdbc-starter</artifactId>
</dependency>
```

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

- Import Dependency

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
</dependency>
```

- Nacos Properties

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

- Import Dependency

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-redis-stater</artifactId>
</dependency>
```

- Nacos Properties

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

## Run Example Application

- spring boot application main function
- docker container  `docker build --build-arg profile=local -t fission-machine-example-service:0.1.0 .`

## Rabbitmq Config

- Import Dependency

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-rabbitmq-stater</artifactId>
</dependency>
```

- Nacos properties

```yaml
spring:
  # rabbitmq
  rabbitmq:
    addresses: localhost:5672
    username: guest
    password: guest
    virtual-host: /
```

- Sender

```java
public class XXXXXX {
    
    private final MessageSender messageSender;
    
    public XXXXXX(MessageSender messageSender) {
        this.messageSender = messageSender;
    }
    
    public Response<PageData<XXXX>> send() {
        // com.devil.fission.machine.rabbitmq.RabbitMqConstants 可以在这里维护使用的交换机，队列，routingKey
        messageSender.sendDirectMessage("交换机名称", "routingKey", "hello world");
    }
    
}
```

- Consumer

```java
@RabbitmqConsumer(queue = "队列名称")
public class RabbitConsumerSample implements MessageConsumerProcess<Object> {
    
    @Override
    public MessageResult process(Object message) {
        log.info(new Gson().toJson(message));
        return MessageResult.success();
    }
}
```

## Sentinel Use

- Import Dependency

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-sentinel-starter</artifactId>
</dependency>
```

- Config

```yaml
# 该配置会在公共配置中添加
spring:
  cloud:
    # sentinel
    sentinel:
      transport:
        #配置Sentinel dashboard地址（根据实际情况修改）
        dashboard: localhost:8080
      #规则持久化从nacos拉取
      datasource:
        # ds1 名称可以随便写
        ds1:
          nacos:
            server-addr: localhost:8848
            # 修改为对应环境的id
            namespace: localhost
            username: nacos
            password: nacos
            # 需要先在sentinel-service后台配置一个流控 或者 在nacos中克隆一份其他服务的
            data-id: ${spring.application.name}-flow-rules
            group-id: DEFAULT
            data-type: json
            rule-type: flow
```

- Add Annotation

```txt
@EnableFissionSentinel
```

## Xxl-Job Use

- Config

```yaml
xxl:
  job:
    accessToken: 'xxxxx'
    admin:
      # 为空关闭自动注册，手动注册
      addresses: xxxxxxx
    executor:
      # 为空关闭自动注册，手动注册
      appname: xxxxxx
      address:
      ip:
      port: 9010
      logpath: /usr/local/xxljob-logs
      logretentiondays: 5
```

- Add XxlJob Annotation

```java
public class TaskHandler {
    
    @XxlJob("batchJobDemo")
    public void batchJobDemo() {
        
    }
}
```

- XxlJob Console Config the Job

## 日志级别动态修改

- 引入包

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-log-starter</artifactId>
</dependency>
```

- 增加或修改配置

```yaml
fission:
  machine:
    log:
      level: INFO
```

- Seata Config

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-seata-starter</artifactId>
</dependency>
```

### 1.数据源配置(默认都是hikari，注意hikari的字段名和默认有区别)

```yaml
spring:
  datasource:
    hikari:
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://xxxx:3306/xxxx?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
      password: xxxx
      username: xxxx
```

### 2.seata配置（xxx-service改成自己服务名）

```yaml
seata:
  # 启用开关
  enabled: true
  service:
    disable-global-transaction: false
    vgroup-mapping:
      # 修改为自己的application-name
      xxx-service: default
      # # 修改为自己的application-name
  tx-service-group: xxx-service
  registry:
    # 通过nacos进行调度
    nacos:
      server-addr: 127.0.0.1:8848
      cluster: default
      namespace: seata
      group: SEATA_GROUP
      username: nacos
      password: nacos
    type: nacos
  config:
    # file模式 比其他模式容易理解，file一般不用改
    type: file
```

### 3.在资源路径下加入file.conf

```text
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  # the client batch send request enable
  enableClientBatchSendRequest = true
  #thread factory for netty
  threadFactory {
    bossThreadPrefix = "NettyBoss"
    workerThreadPrefix = "NettyServerNIOWorker"
    serverExecutorThread-prefix = "NettyServerBizHandler"
    shareBossWorker = false
    clientSelectorThreadPrefix = "NettyClientSelector"
    clientSelectorThreadSize = 1
    clientWorkerThreadPrefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    bossThreadSize = 1
    #auto default pin or 8
    workerThreadSize = "default"
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}
service {
  #transaction service group mapping
  #vgroupMapping.system-service = "default"
  #only support when registry.type=file, please don't set multiple addresses
  #default.grouplist = "127.0.0.1:8091"
  #degrade, current not support
  enableDegrade = false
  #disable seata
  disableGlobalTransaction = false
}

client {
  rm {
    asyncCommitBufferLimit = 10000
    lock {
      retryInterval = 10
      retryTimes = 30
      retryPolicyBranchRollbackOnConflict = true
    }
    reportRetryCount = 5
    tableMetaCheckEnable = false
    reportSuccessEnable = false
  }
  tm {
    commitRetryCount = 5
    rollbackRetryCount = 5
  }
  undo {
    dataValidation = true
    logSerialization = "jackson"
    logTable = "undo_log"
  }
  log {
    exceptionRate = 100
  }
}
```

### 4.spring boot启动类处理(取消jdbc的自动装配)

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

## Redisson Delay Queue Use

### 引入依赖

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-redis-stater</artifactId>
</dependency>
```

### 1.在启动类上加上开关

```text
@EnableRedissonDelayed
```

### 2.实现handler接口

```java

@Component
public class DemoDelayedHandler implements RedissonDelayedHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDelayedHandler.class);
    
    @Override
    public String getQueueName() {
        // 延时队列名称，同一个项目中不可重复
        return "123";
    }
    
    @Override
    public <String> void execute(String s) {
        // 需要用try catch包围，否则队列消费会在jvm层停止
        try {
            String arg = s.toString();
            // 具体的业务逻辑，参数类型可根据业务需要自行替换
            LOGGER.info("参数 = {}", arg);
        } catch (Exception e) {
            LOGGER.error("执行延迟消息失败", e);
        }
    }

}
```

### 3.业务触发消息

```text
// 注入RedissonDelayedUtil
// 入队消息 注意需要与handler中的queueName对应
redissonDelayedUtil.offer("345", 10, TimeUnit.SECONDS, "123");

// 删除消息 注意需要与handler中的queueName对应
redissonDelayedUtil.remove("345", "123");
```

### 4.注意

```text
RedissonDelayedUtil一般会晚于项目当前包的bean的初始化，如果遇到初始化有问题，需要加入@Lazy延迟util初始化
```

## Elasticsearch Use

- 引入依赖

```xml
<dependency>
    <groupId>com.devil.fission</groupId>
    <artifactId>fission-machine-elasticsearch-stater</artifactId>
</dependency>
```

> see https://en.easy-es.cn/pages/7ead0d/