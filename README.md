
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
class SomeActivity extends ActivityBase {
    @AfterPermissionGranted(REQUEST_CODE)
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
}
```

- 集成运行时权限检查以及权限请求相关的api
```java
class SomeActivity extends ActivityBaseWithInitPermission {
    getInitPermissions();
    getInitPermissionsDescriptions();
    onInitPermissionsFinish();
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