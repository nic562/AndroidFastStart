package io.github.nic562.androidFastStart


/**
 * 用于构建显示界面就提示需要授权的基类
 *
 * Created by Nic on 2019/12/19.
 */
abstract class ActivityBaseWithInitPermission : ActivityBase() {

    private var checkingInitPermissionOnce = false

    abstract val initPermissionsRunnable: RunnableWithPermissions

    override fun onStart() {
        super.onStart()
        if (!checkingInitPermissionOnce) {
            initPermissions()
            checkingInitPermissionOnce = true
        }
    }

    /**
     * 启动时检测所需权限
     */
    private fun initPermissions() {
        runWithPermissions(initPermissionsRunnable)
    }
}