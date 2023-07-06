# 公共工程

> 一些项目的公共类，工具，常量等（不依赖框架）

### 目录结构

- com.devil.fission.common
    - exception 公共异常
    - old 之前老项目中用到的工具
    - response 公共响应
    - security 安全认证相关公共类
    - support 特殊支持的公共类
    - util 公共工具类

#### 部分说明

- 该包内不要使用spring的一些注解，和spring web相关的公共配置移步到`service-common`

- 雪花id使用

```markdown
  # 使用前最好设置环境变量`ID_GEN_WORK_ID`和`ID_GEN_DATA_CENTER_ID`以区分不同机器,可避免id重复
  IdGenerator idGenerator = IdGeneratorEnum.INSTANCE.getIdGenerator();
  long nextId = idGenerator.nextId();
```