package io.github.nic562.androidFastStart

import pub.devrel.easypermissions.AfterPermissionGranted

/**
 * 用于构建显示界面就提示需要授权的基类
 *
 * 占用 requestCode 9999
 * Created by Nic on 2019/12/19.
 */
abstract class ActivityBaseWithInitPermission : ActivityBase() {
    companion object {
        private const val REQUEST_CODE_INIT_PERMISSIONS = 9999
    }

    private var finishCheckingInitPermission = false

    override fun onStart() {
        super.onStart()
        if (!finishCheckingInitPermission) {
            initPermissions()
            finishCheckingInitPermission = true
        }
    }

    /**
     * 所需权限列表
     * e.g [android.Manifest.permission.READ_PHONE_STATE]
     */
    abstract fun getInitPermissions(): Array<String>?

    abstract fun getInitPermissionsDescriptions(): String

    abstract fun onInitPermissionsFinish(deniedPermissions: List<String>)

    @AfterPermissionGranted(REQUEST_CODE_INIT_PERMISSIONS)
    private fun initPermissions() { // 启动所需权限
        val perms = getInitPermissions()
        if (perms == null || perms.isEmpty()) {
            return
        }
        if (hasPermissions(*perms)) {
            onInitPermissionsFinish(mutableListOf())
        } else {
            requestPermissions(getInitPermissionsDescriptions(), REQUEST_CODE_INIT_PERMISSIONS, *perms)
        }
    }

    override fun onPermissionsSettingFinish(requestCode: Int, deniedPermissions: List<String>) {
        if (requestCode == REQUEST_CODE_INIT_PERMISSIONS) {
            return onInitPermissionsFinish(deniedPermissions)
        }
        super.onPermissionsSettingFinish(requestCode, deniedPermissions)
    }
}