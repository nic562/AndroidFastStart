
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
    implementation 'com.github.nic562:AndroidFastStart:dev'
}
```

### Activity Start
- 集成运行时权限检查以及权限请求相关的api
```java
class SomeActivity extends ActivityBase {
    getInitPermissions();
    getInitPermissionsDescriptions();
    onInitPermissionsFinish();
    hasPermissions();
    requestPermissions();
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