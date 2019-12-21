## About AndroidFastStart

该项目主要根据个人使用习惯集成一些比较好用的库，以便快速方便搭建新的Android项目开发任务

## Howto

### project build.gradle
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### module build.gradle
```gradle
dependencies {
    implementation 'com.github.nic562:AndroidFastStart:0.6'
}
```

### Start
- 集成 Anko commons组件，可直接使用其AnkoLogger工具 以及 权限请求相关功能
```java
class SomeActivity extends ActivityBase {
    RunnableWithPermissions runnableWithPermissions = new RunnableWithPermissions() {
        String authFailedMsg = "软件所需权限";
        int requestCode = 9999;
        Array<String> permissions = new String[]{android.Manifest.permission.READ_PHONE_STATE};

        @override
        void success() {
            toast("授权成功！");
        }
        
        @override
        void failed(List<String> deniedPermissions) {}
    };
    
    void doSomething() {
        runWithPermissions(runnableWithPermissions);
    }
}
```

- 集成启动时权限检查以及权限请求相关的api
```java
class SomeActivity extends ActivityBaseWithInitPermission {
    RunnableWithPermissions initPermissionsRunnable = new RunnableWithPermissions();
}
```

- 或者集成从拍照、相册中获取图像等api
```java
class SomeActivity extends ActivityBaseWithImageCrop {
    openImageChoice();
    getImageOption();
    onImageReady();
}
```

- 基于 [BRVAH] 封装，使用灵活方便的 RecyclerView，可用于快速构建复杂的数据集展示，自动增量加载等
```kotlin
class SomeActivity : SomethingListable<SomeItem> {
    override val listableManager = object : SomethingListable.ListableManager<SomeItem>() {
        ...
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        
        initListable(recyclerView)
    }
} 
```