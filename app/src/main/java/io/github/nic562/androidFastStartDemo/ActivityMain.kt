package io.github.nic562.androidFastStartDemo

import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBase
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

/**
 * Created by Nic on 2018/10/11.
 */
class ActivityMain: ActivityBase() {

    private val pms = arrayOf(android.Manifest.permission.READ_PHONE_STATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_crop.setOnClickListener {
            startActivity(intentFor<ActivityCropImage>())
        }
    }

    override fun getInitPermissions(): Array<String>? {
        return pms
    }

    override fun getInitPermissionsDescriptions(): String {
        return "软件启动所需权限"
    }

    override fun onInitPermissionsFinish(deniedPermissions: List<String>) {
        if (deniedPermissions.isNotEmpty()){
            toast("授权失败: ${pms.joinToString("\n")}")
        }
    }
}