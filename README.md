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
    implementation 'com.github.nic562:AndroidFastStart:0.9.4.7'
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

- 基于 [BRVAH](https://github.com/CymChad/BaseRecyclerViewAdapterHelper) 封装，使用灵活方便的 RecyclerView，可用于快速构建复杂的数据集展示，自动增量加载等
```kotlin
// 常用列表
class SomeActivity : SomethingListable<SomeItem, Long> {
    override val listableManager = instanceListableManager(args)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // do something
        
        initListable(recyclerView)
    }
    
    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<SomeItem>) {
        // do something
    }
    
    // some where to call ...
    fun f() {
        listableManager.reloadData()
    }
    
}

// 多层次树状列表
class SomeActivity : SomethingTreeListable<Long> {
    override val listableManager = instanceListableManager()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // do something
        
        initListable(recyclerView)
    }
    
    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingTreeListable.OnLoadDataCallback) {
        // do something
    }
    
    // some where to call ...
    fun f() {
        listableManager.reloadData()
    }
    
} 
```

- 整合了 SelectionTracker 用于数据序列的项目批量操作等
```kotlin
class SomeActivity : SomethingListable<SomeItem> {
    override val listableManager = instanceListableManager(args)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // do something
        
        // 可使用 listableManager 中的 setSelectionTracker() 来指定 SelectionTracker 的实现
       
        // 也可通过一个内部的简单实现来指定 
        initListable(recyclerView, withDefaultSelectionTracker = false)
        
        // 获取 SelectionTracker
        listableManager.getSelectionTracker()
    }
    
    override fun getListableItemDetailsProvider(): ItemDetailsProvider<Long>? {
        return object : ItemDetailsProvider<Long> {
            override fun create(): ItemDetails<Long> {
                return object : ItemDetails<Long>() {
                    override fun getSelectionKey(): Long? {
                        return position.toLong()
                    }
                }
            }
        }
    }
    
    // some where to call ...
    fun f() {
        listableManager.reloadData()
    }
    
} 
```