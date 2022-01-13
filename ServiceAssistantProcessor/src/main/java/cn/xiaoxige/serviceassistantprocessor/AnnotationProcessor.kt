package cn.xiaoxige.serviceassistantprocessor

import cn.xiaoxige.serviceassistantannotation.NeedInjected
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * @author xiaoxige
 * @date 4/4/21 9:35 AM
 * -
 * email: xiaoxigexiaoan@outlook.com
 * desc: 注解处理
 */
@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    private lateinit var mElementUtils: Elements
    private lateinit var mFiler: Filer
    private lateinit var mMessage: Messager
    private lateinit var mTypeUtils: Types

    private val mNeedInjectedInfo = mutableMapOf<String, Pair<String, Boolean>>()

    override fun init(p0: ProcessingEnvironment?) {
        requireNotNull(p0) { "p0 cannot be null" }
        super.init(p0)
        p0.run {
            mElementUtils = elementUtils
            mFiler = filer
            mMessage = messager
            mTypeUtils = typeUtils
        }
        i("Service Assistant AnnotationProcessor init")
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NeedInjected::class.java.canonicalName
        )
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        if (p0 == null || p0.isEmpty()) {
            return false
        }
        p0.forEach { element ->
            if (element.qualifiedName.contentEquals(NeedInjected::class.java.canonicalName)) {
                // NeedInjected
                p1?.getElementsAnnotatedWith(NeedInjected::class.java)?.forEach {
                    // NeedInjected 必须是类
                    if (!it.kind.isClass) {
                        e("NeedInjected use in a not class")
                        return false
                    }

                    // NeedInjected 的类不能是抽象和私有的和可继承的
                    val modifiers = it.modifiers
                    if (modifiers.contains(Modifier.ABSTRACT) || modifiers.contains(Modifier.PRIVATE)
                            || modifiers.contains(Modifier.PROTECTED)) {
                        e("NeedInjected class can not abstract or private or protected.")
                        return false
                    }

                    if (!handleNeedInjected(it as TypeElement)) {
                        return false
                    }
                }
            }
        }

        // 打印注册集合
        mNeedInjectedInfo.keys.forEach {
            // FIXME: 2021/6/25 这个return 啥意思？
            val value = mNeedInjectedInfo[it] ?: return@forEach
            i("NeedInjected: $it -> ${value.first} --> ${value.second}")
        }

        // 生成注册信息
        mNeedInjectedInfo.keys.forEach {
            AutoWriteInjectedInfoProducer(it, mNeedInjectedInfo[it], mFiler, mMessage).write()
        }

        // 写完进行清理下
        mNeedInjectedInfo.clear()

        return true
    }

    private fun handleNeedInjected(needInjected: TypeElement): Boolean {
        val interfaces = needInjected.interfaces
        if (interfaces.isEmpty() || interfaces.size > 1) {
            e("Currently, only one interface injection is supported")
        }
        val interfacePath = interfaces[0].toString()
        val annotation = needInjected.getAnnotation(NeedInjected::class.java)
        mNeedInjectedInfo[interfacePath] =
            Pair(needInjected.qualifiedName.toString(), annotation.isSingleCase)
        return true
    }

    companion object{
        private const val TAG = "AnnotationProcessor >>>"

    }
    private fun i(msg: String) {
        mMessage.printMessage(Diagnostic.Kind.NOTE, "$TAG $msg\r\n")
    }

    private fun w(msg: String) =
            mMessage.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "$TAG $msg\r\n")


    private fun e(msg: String) {
        mMessage.printMessage(Diagnostic.Kind.ERROR, "$TAG $msg\r\n")
    }
}