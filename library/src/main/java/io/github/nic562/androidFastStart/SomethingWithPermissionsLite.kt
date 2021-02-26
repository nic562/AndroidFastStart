package io.github.nic562.androidFastStart

import android.app.Activity
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by Nic on 2021/2/26.
 */
interface SomethingWithPermissionsLite : SomethingWithContext, SomethingWithPermissions {

    val permissionCall: PermissionCall

    class PermissionCall(private val ctx: SomethingWithPermissionsLite) : EasyPermissions.PermissionCallbacks {
        private val isInActivity = ctx.getOwnerContext() is Activity

        private val deniedPermissions = ArrayList<String>()
        private val mapRunnableWithPermissions = mutableMapOf<Int, SomethingWithPermissions.RunnableWithPermissions>()

        private var latestPermissionSettingRequestCode: Int = -1

        private fun requestPermissions(msg: String, requestCode: Int, vararg perms: String) {
            if (isInActivity) {
                EasyPermissions.requestPermissions(ctx.getOwnerContext() as Activity, msg, requestCode, *perms)
            } else {
                EasyPermissions.requestPermissions(ctx.getOwnerFragment(), msg, requestCode, *perms)
            }
        }

        private fun onPermissionsSettingFinish(requestCode: Int) {
            if (requestCode in mapRunnableWithPermissions) {
                mapRunnableWithPermissions.remove(requestCode)
            }
            latestPermissionSettingRequestCode = -1
        }

        private fun somePermissionPermanentlyDenied(perms: List<String>): Boolean {
            if (isInActivity) {
                return EasyPermissions.somePermissionPermanentlyDenied(ctx.getOwnerContext() as Activity, perms)
            }
            return EasyPermissions.somePermissionPermanentlyDenied(ctx.getOwnerFragment(), perms)
        }

        /**
         * 部分拒绝授权也会调用
         */
        override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
            deniedPermissions.clear()
            deniedPermissions.addAll(perms)
            // 对应权限没有被授权并且设置为不再询问的, 再次打开对话框提醒用户
            if (somePermissionPermanentlyDenied(perms)) {
                latestPermissionSettingRequestCode = requestCode // 保持这个请求号，以便子类再继续处理该情况
                if (isInActivity) {
                    AppSettingsDialog.Builder(ctx.getOwnerContext() as Activity).build().show()
                } else {
                    AppSettingsDialog.Builder(ctx.getOwnerFragment()).build().show()
                }
            } else {
                val r = mapRunnableWithPermissions[requestCode]
                if (r != null) {
                    r.failed(perms)
                    onPermissionsSettingFinish(requestCode)
                }
            }
        }

        /**
         * 部分授权也会调用
         */
        override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
            val r = mapRunnableWithPermissions[requestCode]
            if (r != null) {
                if (r.permissions.size == perms.size) {
                    r.success()
                    onPermissionsSettingFinish(requestCode)
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
        }

        fun requestPermissions(runnableWithPermissions: SomethingWithPermissions.RunnableWithPermissions) {
            requestPermissions(
                    runnableWithPermissions.authFailedMsg,
                    runnableWithPermissions.requestCode,
                    *runnableWithPermissions.permissions)
            mapRunnableWithPermissions[runnableWithPermissions.requestCode] = runnableWithPermissions
        }

        fun onActivityResult(requestCode: Int) {
            if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && latestPermissionSettingRequestCode != -1) {
                /**
                 * 在重复提示进入系统设置中设置权限后的回调, 检测没授权的权限列表
                 *
                 * 从设置项返回, resultCode 都是cancel, 必须重新自行判断
                 */
                val dms = ArrayList<String>()
                for (x in deniedPermissions) {
                    if (!ctx.hasPermissions(x)) {
                        dms.add(x)
                    }
                }
                deniedPermissions.clear()
                if (dms.size > 0) {
                    onPermissionsDenied(latestPermissionSettingRequestCode, dms)
                } else {
                    val r = mapRunnableWithPermissions[latestPermissionSettingRequestCode]
                    if (r != null) {
                        onPermissionsSettingFinish(latestPermissionSettingRequestCode)
                        ctx.runWithPermissions(r)
                    }
                }
            }
        }
    }

    /**
     * 如果你需要在Fragment中使用此接口，你需要重写该方法
     */
    fun getOwnerFragment(): Fragment {
        throw NotImplementedError("you need to override the function `getOwnerFragment()`! on ${getOwnerContext()}")
    }

    override fun runWithPermissions(runnableWithPermissions: SomethingWithPermissions.RunnableWithPermissions) {
        if (hasPermissions(*runnableWithPermissions.permissions)) {
            runnableWithPermissions.success()
        } else {
            permissionCall.requestPermissions(runnableWithPermissions)
        }
    }

    override fun hasPermissions(vararg permissions: String): Boolean {
        if (permissions.isEmpty()) {
            return true
        }
        return EasyPermissions.hasPermissions(getOwnerContext(), *permissions)
    }
}