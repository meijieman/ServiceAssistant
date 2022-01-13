package cn.xiaoxige.serviceassistantplugin.util

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

/**
 * @author xiaoxige
 * @date 4/2/21 11:56 PM
 * -
 * email: xiaoxigexiaoan@outlook.com
 * desc: 打印日志的类
 */
object Logger {
    private const val TAG = "Plugin>>>"

    private lateinit var sLogger: org.gradle.api.logging.Logger

    fun make(project: Project) {
        // OutputEventListenerBackedLogger
        sLogger = project.logger
        // FIXME: 2021/6/29 设置日志级别 info，没有生效？
        project.logging.captureStandardOutput(LogLevel.INFO)

        /*
           Logger 日志级别分 DEBUG,INFO,LIFECYCLE,WARN,QUIET,ERROR; 默认 LIFECYCLE 级别，默认不打印 debug，info 日志
           1. 需要打印 debug 日志可以命令运行 ./gradlew --debug assembleDebug
           2. 可以在 gradle.properties 配置 org.gradle.logging.level=info
           3. 代码中设置 project.logging.captureStandardOutput(LogLevel.INFO)
         */
        w("info, warn [${sLogger.isInfoEnabled}, ${sLogger.isWarnEnabled}]")
    }

    fun w(msg: String) = sLogger.warn("$TAG $msg")

    fun e(msg: String) = sLogger.error("$TAG $msg")
}