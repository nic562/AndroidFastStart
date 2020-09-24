package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingWithImageCrop
import kotlinx.android.synthetic.main.activity_crop_image.*
import java.io.File

/**
 * Created by Nic on 2020/9/24.
 */
class ActivityCropImage2 : ActivityBase(),
        SomethingWithImageCrop {
    override var tmpCameraSavingFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        btn_crop.setOnClickListener {
            openImageChoice()
        }
    }

    override fun onImageReady(tmpFilePath: String) {
        tv_msg.text = tmpFilePath
        iv_marker.setImageURI(Uri.fromFile(File(tmpFilePath)))
    }

    override fun getTmpDir(): File {
        return File(getExternalStoragePath("cropTmp"))
    }

    override fun getImageOption(): SomethingWithImageCrop.ImageOption {
        return SomethingWithImageCrop.ImageOption().apply {
            crop = true
            compressQuality = 80
        }
    }

    private fun openImageChoice() {
        runWithPermissions(getImageChoiceRunnableWithPermissions())
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onImageChoiceActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        clearImageCroppingTmp()
        super.onDestroy()
    }
}