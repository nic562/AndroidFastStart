package io.github.nic562.androidFastStart

import android.content.Context
import android.content.Intent
import java.io.File

/**
 * 集成从拍照、相册中获取图像、截图等api
 *
 * 占用 requestCode
 * - 9998
 * - 9997
 * - 9996
 * - 69
 *
 * Created by Nic on 2018/10/10.
 */
abstract class ActivityBaseWithImageCrop : ActivityBase(),
    SomethingWithImageCrop {
    override var tmpCameraSavingFile: File? = null

    protected fun openImageChoice() {
        runWithPermissions(getImageChoiceRunnableWithPermissions())
    }

    override fun getTmpDir(): File {
        return File(getExternalStoragePath("cropTmp"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onImageChoiceActivityResult(requestCode, resultCode, data)
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onDestroy() {
        clearImageCroppingTmp()
        super.onDestroy()
    }
}