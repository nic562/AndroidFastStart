package io.github.nic562.androidFastStartDemo

import android.net.Uri
import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBaseWithImageCrop
import io.github.nic562.androidFastStart.SomethingWithImageCrop
import kotlinx.android.synthetic.main.activity_crop_image.*
import java.io.File

/**
 * Created by Nic on 2018/10/11.
 */
class ActivityCropImage : ActivityBaseWithImageCrop() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        btn_crop.setOnClickListener {
            openImageChoice()
        }
    }

    override fun getImageOption(): SomethingWithImageCrop.ImageOption {
        val o = SomethingWithImageCrop.ImageOption()
        o.crop = true
        o.compressQuality = 80
        return o
    }

    override fun onImageReady(tmpFilePath: String) {
        tv_msg.text = tmpFilePath
        iv_marker.setImageURI(Uri.fromFile(File(tmpFilePath)))
    }
}