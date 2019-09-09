package com.gaofu.complier;

import com.gaofu.annotation.BindView;
import com.gaofu.annotation.OnClick;
import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author Gaofu
 * Time 2019-09-06 14:58
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    // 用来报告错误,警告和其他提示信息
    private Messager messager;
    // Elements 中包含用于操作 Element 的工具方法
    private Elements elementUtil;
    // 用来创建新的源文件,class 文件以及辅助文件
    private Filer filer;
    // Types 中包含用于操作 TypeMirror 的工具方法
    private Types typeUtils;
    private String activityName;

    // 该方法主要用于一些初始化的操作,通过方法的参数 ProcessingEnvironment 可以获取一系列有用的工具类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtil = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        messager.printMessage(Diagnostic.Kind.NOTE, "init -------->");
    }

    // 想让注解处理器处理那些注解(@BindView @OnClick)
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        // 添加支持 BindView 注解的类型
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

    // 什么版本的 JDK 进行编译 (java - javac - class)
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        // android.util.Log.i(TAG, "start ->");
        messager.printMessage(Diagnostic.Kind.NOTE, "start------------------------->");

        //获取 MainActivity 中所有带 BindView 注解的属性
        Set<? extends Element> bindViewSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 保存键值对,key 是 com.gaofu.butterknife.MainActivity  value 是所有带 BindView 注解的属性集合
        Map<String, List<VariableElement>> bindViewMap = new HashMap<>();
        // 遍历所有带 BindView 注解的属性
        for (Element element : bindViewSet) {
            // 转成原始属性元素(结构体元素)
            VariableElement variableElement = (VariableElement) element;
            // 通过属性元素获取它所属的 MainActivity 类名,如:com.gaofu.butterknife.MainActivity
            activityName = getActivityName(variableElement);
            // 从缓存集合中获取 MainActivity 所有带 BindView 注解的属性集合
            List<VariableElement> list = bindViewMap.get(activityName);
            if (null == list) {
                list = new ArrayList<>();
                // 先加入 map 集合,引用变量 list 可以动态改变值
                bindViewMap.put(activityName, list);
            }
            // 将 MainActivity 所有带 BindView 注解的属性加入到 list 集合
            list.add(variableElement);
            // 测试打印:每个属性名字
            messager.printMessage(Diagnostic.Kind.NOTE, "variableElement >>> " + variableElement.getSimpleName().toString());
        }

        // 获取 MainActivity 中所有带 OnClick 的注解方法
        Set<? extends Element> onClickSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        // 保存键值对,key 是 com.gaofu.butterknife.MainActivity value 是所有带 OnClick 注解的方法集合
        Map<String, List<ExecutableElement>> onClickMap = new HashMap<>();
        // 遍历所有带 OnClick 注解的方法
        for (Element element : onClickSet) {
            // 转成原始属性元素(结构体元素)
            ExecutableElement executableElement = (ExecutableElement) element;
            // 通过属性元素获取它所属的 MainActivity 类名,如:com.gaofu.butterknife.MainActivity
            activityName = getActivityName(executableElement);
            // 从缓存集合中获取 MainActivity 所有带 OnClick 注解的方法集合
            List<ExecutableElement> list = onClickMap.get(activityName);
            if (null == list) {
                list = new ArrayList<>();
                // 先加入 map 集合,引用变量 list 可以动态改变值
                onClickMap.put(activityName, list);
            }
            // 将 MainActivity 所有带 OnClick 注解的属性加入到 list 集合
            list.add(executableElement);
            // 测试打印:每个方法的名字
            messager.printMessage(Diagnostic.Kind.NOTE, "executableElement >>> " + executableElement.getSimpleName().toString());
        }

        //-------------------造币过程-------------------
        // 获取 Activity 完整的字符串类名(包名+类名)
        // 获取 "com.gaofu.butterknife.MainActivity" 中所有控件属性的集合
        List<VariableElement> cacheElements = bindViewMap.get(activityName);
        List<ExecutableElement> clickElements = onClickMap.get(activityName);

        try {
            // 创建一个新的源文件(Class),并返回一个对象以允许写入它
            JavaFileObject javaFileObject = filer.createSourceFile(activityName + "$ViewBinder");
            // 通过属性标签获取包名标签(任意一个属性标签的父节点都是同一个包名)
            String packageName = getPackageName(cacheElements.get(0));
            // 定义 Writer 对象,开启造币过程
            Writer writer = javaFileObject.openWriter();

            // 类名:MainActivity$ViewBinder, 不是 com.gaofu.butterknife.MainActivity$ViewBinder
            // 通过属性元素获取它所属的 MainActivity 类名,再拼接后结果为:MainActivity$ViewBinder
            String activitySimleName = cacheElements.get(0).getEnclosingElement().getSimpleName().toString() + "$ViewBinder";

            messager.printMessage(Diagnostic.Kind.NOTE, "activityName >>> " + activityName + " / activitySimpleName >>> " + activitySimleName);

            // 第一行生成包
            writer.write("package " + packageName + ";\n");
            // 第二行生成要导入的接口类(必须手动导入)
            writer.write("import com.gaofu.library.ViewBinder;\n");
            writer.write("import com.gaofu.library.DebouncingOnClickListener;\n");
            writer.write("import android.view.View;\n");

            // 第三行生成类
            writer.write("public class " + activitySimleName + " implements ViewBinder<" + activityName + "> {\n");
            writer.write("@Override\npublic void bind(final " + activityName + " target) {\n");

            // 循环生成 MainActivity 每个控件属性
            for (VariableElement variableElement : cacheElements) {
                // 控件属性名
                String fieldName = variableElement.getSimpleName().toString();
                // 获取控件的注解
                BindView bindView = variableElement.getAnnotation(BindView.class);
                // 获取控件注解的 id 值
                int id = bindView.value();
                // 生成:target.tv = target.findViewById(xxx);
                writer.write("target." + fieldName + " = " + "target.findViewById(" + id + ");\n");
            }

            // 循环生成 MainActivity 每个点击事件
            for (ExecutableElement executableElement : clickElements) {
                // 获取方法名
                String methodName = executableElement.getSimpleName().toString();
                // 获取方法的注解
                OnClick onClick = executableElement.getAnnotation(OnClick.class);
                // 获取方法注解的 id 值
                int id = onClick.value();
                // 获取方法参数
                List<? extends VariableElement> parameters = executableElement.getParameters();

                // 生成点击事件
                writer.write("target.findViewById(" + id + ").setOnClickListener(new DebouncingOnClickListener() {\n");
                writer.write("public void doClick(View view) {\n");
                if (parameters.isEmpty()) {
                    writer.write("target." + methodName + "();\n}\n});\n");
                } else {
                    writer.write("target." + methodName + "(view);\n}\n});\n");
                }
            }

            //最后结束便签,造币完成
            writer.write("\n}\n}");
            writer.close();

            messager.printMessage(Diagnostic.Kind.NOTE, "end----------------------->");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getPackageName(Element executableElement) {
        // 通过方法标签获取类名标签
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        // 通过类名标签获取包名标签
        String packageName = elementUtil.getPackageOf(typeElement).getQualifiedName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, "packageName >>> " + packageName);
        return packageName;
    }

    private String getActivityName(Element executableElement) {
        // 通过方法标签获取类名标签,再通过类名标签获取包名标签
        String packageName = getPackageName(executableElement);
        // 通过方法标签获取类名标签
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        // 完整字符串拼接:com.gaofu.butterknife + "." + MainActivity
        return packageName + "." + typeElement.getSimpleName().toString();
    }

}
