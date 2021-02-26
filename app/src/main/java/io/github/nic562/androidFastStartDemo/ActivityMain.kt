package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.nic562.androidFastStart.SomethingWithPermissions
import io.github.nic562.androidFastStart.SomethingWithPermissionsLite
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

/**
 * Created by Nic on 2018/10/11.
 */
class ActivityMain : AppCompatActivity(), SomethingWithPermissionsLite {
    override val permissionTool by lazy { initPermissionTool(this) }

    override fun getOwnerContext(): Context {
        return this
    }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionTool.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionTool.onActivityResult(requestCode)
    }

    override fun onStart() {
        super.onStart()
        runWithPermissions(initPermissionsRunnable)
    }

    private val initPermissionsRunnable = object : SomethingWithPermissions.RunnableWithPermissions {
        override val authFailedMsg = "软件启动所需权限"
        override val requestCode = 9999
        override val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        override fun success() {
            toast("授权成功！")
        }

        override fun failed(deniedPermissions: List<String>) {
            toast("授权失败: ${deniedPermissions.joinToString("\n")}")
            finish()
        }
    }
}