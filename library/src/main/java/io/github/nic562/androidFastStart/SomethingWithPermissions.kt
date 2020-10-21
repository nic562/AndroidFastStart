package io.github.nic562.androidFastStart

/**
 * Created by Nic on 2020/10/21.
 */
interface SomethingWithPermissions {

    interface RunnableWithPermissions {
        val authFailedMsg: String
        val requestCode: Int
        val permissions: Array<String>
        fun success()
        fun failed(deniedPermissions: List<String>) {}
    }

    fun runWithPermissions(runnableWithPermissions: RunnableWithPermissions)

    fun hasPermissions(vararg permissions: String): Boolean
}