# 笔记
## 项目依赖关系

ServiceAssistantPlugin

ServiceAssistantAnnotation

ServiceAssistantProcessor -> ServiceAssistantAnnotation
ServiceAssistantCore -> ServiceAssistantAnnotation

LoginComponent -> ServiceAssistantCore,LoginApi
AccountComponent -> ServiceAssistantCore,ServiceAssistantProcessor,AccountApi,LoginApi,AppApi

LoginApi
AppApi
AccountApi

app -> ServiceAssistantCore,AppApi,LoginApi,AccountApi,ServiceAssistantProcessor,LoginComponent,AccountComponent

## ServiceAssistantPlugin gradle 插件，在生成 dex 前对 class 做处理
项目中用于实现自动注入变量

Gradle 插件 + ASM 实战

### 调试方法，在本地生成 jar 后本地依赖
```
    repositories {
        maven {
            url uri('./plugin')
        }
//        maven {
//            url "https://gitee.com/xiaoxigexiaoan/warehouse/raw/master"
//        }
        google()
    }
```

## ServiceAssistantProcessor 编译时注解处理


## org.gradle.api.logging.Logger 日志级别设置
默认级别 LIFECYCLE
参考 https://www.cnblogs.com/skymxc/p/gradle-logger.html

## class 中织入代码不会影响项目中代码的行数，为什么？

## 假如织入的代码有异常，如何定位

## gradle plugin 调试方式


















