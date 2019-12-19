
### root build.gradle
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
    implementation 'com.github.nic562:AndroidFastStart:0.5'
}
```

### Activity Start
- 集成 Anko commons组件，可直接使用其AnkoLogger工具 以及 权限请求相关功能
```java
/**
* 不建议这样写，已经简化了流程请参考下一段代码
*/
class SomeActivity extends ActivityBase {
    public doSomething() {
        debug("debug...");
        info("info...");
        error("error...");
        toast("toast...");
        if (hasPermissions(permissions)){
            // TODO
        } else {
            requestPermissions(permissions);
        }
    }
    @override
    public void onPermissionsGranted(){}
    @Override
    public void onPermissionsDenied() {}
    
}

/**
* 建议这样写
*/
class SomeActivity extends ActivityBase {
    RunnableWithPermissions runnableWithPermissions = new RunnableWithPermissions() {
        String authFailedMsg = "软件所需权限";
        int requestCode = 9999;
        Array<String> permissions = new String[]{android.Manifest.permission.READ_PHONE_STATE};

        @override
        void success() {
            toast("授权成功！");
        }
    };
    
    void doSomething() {
        runWithPermissions(runnableWithPermissions);
    }
}
```

- 集成运行时权限检查以及权限请求相关的api
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