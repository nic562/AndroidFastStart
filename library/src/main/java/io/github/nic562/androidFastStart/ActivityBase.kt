package io.github.nic562.androidFastStart

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.AnkoLogger

/**
 * 用于快速构建activity的基类，集成了Anko Logger，以及EasyPermissions
 *
 * 子类中可直接使用
 *
 * AnkoLogger #verbose, debug, info, warn, error, wtf 等日志记录方法
 *
 * @see AnkoLogger
 *
 *
 *
 * Created by Nic on 2018/10/10.
 */
abstract class ActivityBase : AppCompatActivity(),
        SomethingWithPermissionsLite,
        AnkoLogger {

    override val permissionCall = SomethingWithPermissionsLite.PermissionCall(this)

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionCall.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionCall.onActivityResult(requestCode)
    }

    /**
     * 获取内部存储根路径 受系统保护的目录
     * 需要 READ_PHONE_STATE 该权限在android M 之后不再默认授权 * 注意调用该方法时必须先检查权限
     * 因为新的系统对权限的管理有区别于旧系统，不能设置成全局静态变量，否则在高版本系统运行时会因为没有权限而崩溃
     */
    open fun getInternalStoragePath(): String {
        return applicationContext.filesDir.absolutePath
    }

    /**
     * 获取外部存储根路径 一般在外接SD卡
     * 注意事项参考[getInternalStoragePath]
     */
    open fun getExternalStoragePath(dirName: String? = null): String {
        return applicationContext.getExternalFilesDir(dirName)?.absolutePath
                ?: throw RuntimeException("Path to `$dirName` open failed!")
    }
}