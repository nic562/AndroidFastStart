package io.github.nic562.androidFastStart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.AnkoLogger
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

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
abstract class ActivityBase : AppCompatActivity(), AnkoLogger, EasyPermissions.PermissionCallbacks {

    private val deniedPermissions = ArrayList<String>()

    private var latestPermissionSettingRequestCode: Int = 0

    protected fun hasPermissions(vararg permissions: String): Boolean {
        return EasyPermissions.hasPermissions(this, *permissions)
    }

    protected fun requestPermissions(msg: String, requestCode: Int, vararg perms: String) {
        EasyPermissions.requestPermissions(this, msg, requestCode, *perms)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        deniedPermissions.clear()
        deniedPermissions.addAll(perms)
        // 对应权限没有被授权并且设置为不再询问的, 再次打开对话框提醒用户
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            latestPermissionSettingRequestCode = requestCode // 保持这个请求号，以便子类再继续处理该情况
            AppSettingsDialog.Builder(this).build().show()
        } else {
            onPermissionsSettingFinish(requestCode, perms)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // 从设置项返回, resultCode 都是cancel, 必须重新自行判断
            val dms = ArrayList<String>()
            for (x in deniedPermissions) {
                if (!EasyPermissions.hasPermissions(this, x)) {
                    dms.add(x)
                }
            }
            deniedPermissions.clear()
            onPermissionsSettingFinish(latestPermissionSettingRequestCode, dms)
        }
    }

    /**
     * 在重复提示进入系统设置中设置权限后的回调, 返回仍然没授权的权限列表
     */
    protected open fun onPermissionsSettingFinish(requestCode: Int, deniedPermissions: List<String>) {
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