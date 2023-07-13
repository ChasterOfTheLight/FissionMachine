# FissionMachine
A machine that meets the requirements of project fission and is suitable for quickly building a Spring Cloud scaffolding project.Using these tools and technologies, you can quickly set up a Spring Cloud project and meet the requirements of project fission, allowing for efficient development and scalability.

## Common Config

```yaml
# common-config.yaml
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
#mybatis-plus
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    # 开启打印sql，不配置不打印
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```