package cn.xiaoxige.serviceassistantprocessor

import cn.xiaoxige.serviceassistantprocessor.util.getPackageAndClassName
import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic

/**
 * @author xiaoxige
 * @date 4/6/21 9:29 PM
 * -
 * email: xiaoxigexiaoan@outlook.com
 * desc: 自动生成注入信息
 */
class AutoWriteInjectedInfoProducer(
    private val injectedInterface: String,
    private val needInjectedInfo: Pair<String, Boolean>?,
    private val filer: Filer,
    private val mMessage: Messager
) {

    fun write() {
        // 生成类相关的信息
        // cn.xiaoxige.accountcomponent.repo.IAccountRepo, cn.xiaoxige.serviceassistant.repo.ISettingRepo, cn.xiaoxige.serviceassistant.repo.IAboutRepo
        i("injectedInterface $injectedInterface")
        val injectedInfoProducerFullClass = getInjectedProducerClassFullName()
        val injectedInfoProducerFullClassInfo: Pair<String, String> =
            injectedInfoProducerFullClass.getPackageAndClassName()

        val packageName = injectedInfoProducerFullClass.substring(0, injectedInfoProducerFullClass.lastIndexOf("."))
        val className = injectedInfoProducerFullClass.substring(injectedInfoProducerFullClass.lastIndexOf(".") + 1)

        // 目标接口信息
        val injectedInterfaceInfo: Pair<String, String> = injectedInterface.getPackageAndClassName()

        // 注解 需要使用 androidx.annotation.Keep
        val annotation = AnnotationSpec.builder(ClassName.get("androidx.annotation", "Keep")).build()

        // 属性
        val field = createField(injectedInterfaceInfo.first, injectedInterfaceInfo.second)
        val lockField = createLockField()

        // 方法
        val method = createMethod(injectedInterfaceInfo)

        val autoClass = TypeSpec.classBuilder(injectedInfoProducerFullClassInfo.second)
            .addJavadoc("This class is a Service Assistant Processor transfer center class.\n which is automatically generated. Please do not make any changes.\n")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(annotation)
            .addField(lockField)
            .addField(field)
            .addMethod(method)
            .build()

        JavaFile.builder(injectedInfoProducerFullClassInfo.first, autoClass)
            .build()
            .writeTo(filer)
        i("生成文件完成 packageName ${injectedInfoProducerFullClassInfo.first}, 源代码 $autoClass")
    }

    private fun createField(packageInfo: String, className: String): FieldSpec {
        return FieldSpec.builder(ClassName.get(packageInfo, className), NAME_TARGET_INSTANCE)
            .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
            .addJavadoc("target entity class")
            .initializer("null")
            .build()
    }

    private fun createLockField(): FieldSpec {
        return FieldSpec.builder(Any::class.java, "sLock", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
            .addJavadoc("Changed mainly for lock guarantee instance\n")
            // 对应 java 代码 .initializer("new $T()", Object.class)
            // ${'$'} 是对 $ 转义
            .initializer("""new ${'$'}T()""", Any::class.java)
//            .initializer("new \$T()", Any::class.java)
            .build()
    }

    private fun createMethod(injectedInterfaceInfo: Pair<String, String>): MethodSpec {
        val methodSpaceBuilder = MethodSpec.methodBuilder(NAME_GET_TARGET_INSTANCE_METHOD)
            .addJavadoc("How to get the target instance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(injectedInterfaceInfo.first, injectedInterfaceInfo.second))

        // 如果未发现, 那么直接返回 null
        if (needInjectedInfo == null) {
            return methodSpaceBuilder.addStatement("return null").build()
        }

        // 生成目标对象的信息
        val needInjectedInterfaceInfo = needInjectedInfo.first.getPackageAndClassName()
        // 如果为非单例, 那么每次都会产生一个新对象
        if (!needInjectedInfo.second) {
            return methodSpaceBuilder.addStatement(
                """return new ${'$'}T()""",
                ClassName.get(needInjectedInterfaceInfo.first, needInjectedInterfaceInfo.second))
                .build()
        }

        // 单例模式
        methodSpaceBuilder.beginControlFlow("if($NAME_TARGET_INSTANCE != null)")
        methodSpaceBuilder.addStatement("return $NAME_TARGET_INSTANCE")
        methodSpaceBuilder.endControlFlow()

        methodSpaceBuilder.beginControlFlow("synchronized(sLock)")
        // 再次判断是否为空
        methodSpaceBuilder.beginControlFlow("if($NAME_TARGET_INSTANCE != null)")
        methodSpaceBuilder.addStatement("return $NAME_TARGET_INSTANCE")
        methodSpaceBuilder.endControlFlow()

        methodSpaceBuilder.addStatement(
            """$NAME_TARGET_INSTANCE = new ${'$'}T()""",
            ClassName.get(needInjectedInterfaceInfo.first, needInjectedInterfaceInfo.second)
        )
        methodSpaceBuilder.addStatement("return $NAME_TARGET_INSTANCE")

        methodSpaceBuilder.endControlFlow()

        return methodSpaceBuilder.build()
    }

    private fun getInjectedProducerClassFullName(): String = "${injectedInterface}Producer"

    companion object {
        private const val NAME_TARGET_INSTANCE = "sInstance"
        private const val NAME_GET_TARGET_INSTANCE_METHOD = "getInstance"
        private const val TAG = "AutoWriteInjectedInfoPr >>>"
    }

    private fun i(msg: String) = mMessage.printMessage(Diagnostic.Kind.NOTE, "$TAG $msg\r\n")

    private fun w(msg: String) = mMessage.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "$TAG $msg\r\n")

    private fun e(msg: String) = mMessage.printMessage(Diagnostic.Kind.ERROR, "$TAG $msg\r\n")

}