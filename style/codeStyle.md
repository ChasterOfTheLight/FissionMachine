# 代码风格说明

## 代码风格

编码规范遵从于《阿里巴巴JAVA开发规约》和社区制定

## 相关文件

### Idea IDE

代码风格文件在源代码下的`style/fission-machine-code-style-for-idea.xml`文件中，开发者可以将其导入到Idea IDE中，并让IDE帮助您格式化代码。

#### 导入方式

```
Preferences/Settings --> Editor --> Code Style --> Schema --> Import Schema --> IntelliJ IDEA code style XML
```

[checkstyle插件idea安装](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)

1. `Preferences/Settings --> Other Settings --> Checkstyle` 或者 `Preferences/Settings --> Tools --> Checkstyle`
2. 在checkstyle插件中设置checkstyle版本为8.30,并将扫描作用域设置为`All resource(including tests)`
3. 导入源代码下`style/FissionMachineCheckStyle.xml`文件到checkstyle插件。作用范围*项目文件*即可
4. 用checkstyle插件扫描你修改的代码。