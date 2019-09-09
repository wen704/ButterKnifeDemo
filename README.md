# 注解器使用,小型 ButterKnife
[TOC]

源码中有大量注释供参考
### 创建 Java Library, 分别为 `annotation` 和 `complier`
>添加 Java Library 下 build.gradle 编码

```gradle
tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}
```

>添加 `complier` 注解服务并引入要解析的注解 module

```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])

    compile 'com.google.auto.service:auto-service:1.0-rc6'
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'

    implementation project(":annotation")
}
``` 
#### 于 `annotation` 创建 `BindView` 和 `OnClick` 注解
```java
// BindView.java
@Target(ElementType.FIELD)// 该注解作用在属性之上
@Retention(RetentionPolicy.CLASS)// 编译器工作,通过注解处理器
public @interface BindView {
    // 返回 R.id.xx
    int value();
}

// OnClick.java
@Target(ElementType.METHOD)// 该注解作用在方法之上
@Retention(RetentionPolicy.CLASS)// 编译器工作,通过注解处理器
public @interface OnClick {
    // 返回 R.id.xx
    int value();
}
```

#### 于 `complier` 创建 `ButterKnifeProcessor`
```java
// ButterKnifeProcessor.java
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        return false;
    }
}
```

### 创建 Android Library, `library` 用于辅助注解器生成的文件
>辅助生成一个 类名+"$ViewBinder" 的类,用于实现控件的初始化以及点击事件实现,代码形式如下

```java
public class MainActivity$ViewBinder implements ViewBinder<com.gaofu.butterknifedemo.MainActivity> {
    @Override
    public void bind(final com.gaofu.butterknifedemo.MainActivity target) {
        target.tv = target.findViewById(2131165326);
        target.btn = target.findViewById(2131165218);
        target.findViewById(2131165218).setOnClickListener(new DebouncingOnClickListener() {
            public void doClick(View view) {
                target.click();
            }
        });

    }
}
```

#### 于 `library` 下创建 `ButterKnife` , `DebouncingOnClickListener` 和 `ViewBinder`
```java
// ViewBinder.java
public interface ViewBinder<T> {
    void bind(T target);
}

// ButterKnife.java
public class ButterKnife {
    public static void bind(Activity activity) {
        // 拼接一个类名 MainActivity$ViewBinder
        String className = activity.getClass().getName() + "$ViewBinder";
        try {
            // ViewBinder 接口的实现类
            Class clazz = Class.forName(className);
            // 接口 = 接口的实现类
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();
            // 调用接口的 bind 方法
            viewBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// DebouncingOnClickListener.java
public abstract class DebouncingOnClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        doClick(view);
    }

    public abstract void doClick(View v);
}
```

### 在主工程下添加相应依赖,并使用注解(注解器内的实现暂未实现),示例:
```gradle
// 主工程 build.gradle
dependencies {
    ...
    implementation project(":library")
    implementation project(":annotation")
    annotationProcessor project(":complier")
}
```

```java
// 主工程 MainActivity.java
public class MainActivity extends Activity {
    // 使用刚创建的注解初始化控件
    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.btn)
    Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 注解辅助
        ButterKnife.bind(this);
    }
    
    // 使用注解实现控件的点击事件
    @OnClick(R.id.btn)
    public void click() {
        Toast.makeText(this, btn.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
```

#### 注解处理器代码实现
>于 `processor` 中的 `init` 方法中初始化工具

```java
// 用来报告错误,警告和其他提示信息
private Messager messager;
// Elements 中包含用于操作 Element 的工具方法
private Elements elementUtil;
// 用来创建新的源文件,class 文件以及辅助文件
private Filer filer;

@Override
public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    elementUtil = processingEnvironment.getElementUtils();
    messager = processingEnvironment.getMessager();
    filer = processingEnvironment.getFiler();
}
```

>于 `processor` 中的 `getSupportedAnnotationTypes` 方法中设置注解器需要解析的注解

```java
@Override
public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(BindView.class.getCanonicalName());
    types.add(OnClick.class.getCanonicalName());
    return types;
}
```

>于 `processor` 中的 `getSupportedSourceVersion` 方法中设置注解器编译的 JDK

```java
@Override
public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_7;
}
```

>于 `processor` 中的 `process` 方法中处理对应的注解以及生成辅助类文件
>遍历所有 `BindView` 和 `OnClick` 注解并存放在集合中
>通过 `Filer` 工具,创建辅助类文件,并在其中实现控件的初始化以及点击事件的实现

[ButterKnifeProcessor源码](https://github.com/wen704/ButterKnifeDemo/blob/master/complier/src/main/java/com/gaofu/complier/ButterKnifeProcessor.java)



