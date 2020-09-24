package io.github.nic562.androidFastStartDemo

import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBaseWithInitPermission
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

/**
 * Created by Nic on 2018/10/11.
 */
class ActivityMain : ActivityBaseWithInitPermission() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_crop.setOnClickListener {
            startActivity(intentFor<ActivityCropImage>())
        }

        btn_crop2.setOnClickListener {
            startActivity(intentFor<ActivityCropImage2>())
        }

        btn_sms.setOnClickListener {
            startActivity(intentFor<ActivitySMS>())
        }

        btn_card.setOnClickListener {
            startActivity(intentFor<ActivityCard>())
        }

        btn_card_raw.setOnClickListener {
            startActivity(intentFor<ActivityCardRaw>())
        }

        btn_Tree.setOnClickListener {
            startActivity(intentFor<ActivityTree>())
        }

        btn_page.setOnClickListener {
            startActivity(intentFor<ActivityPage>())
        }

        btn_coll.setOnClickListener {
            startActivity(intentFor<ActivityColl>())
        }
    }

    override val initPermissionsRunnable = object : RunnableWithPermissions {
        override val authFailedMsg = "软件启动所需权限"
        override val requestCode = 9999
        override val permissions = arrayOf(android.Manifest.permission.READ_PHONE_STATE)

        override fun success() {
            toast("授权成功！")
        }

        override fun failed(deniedPermissions: List<String>) {
            toast("授权失败: ${deniedPermissions.joinToString("\n")}")
            finish()
        }
    }
}