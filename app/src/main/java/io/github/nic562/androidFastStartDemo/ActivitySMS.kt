package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import androidx.recyclerview.widget.RecyclerView
import io.github.nic562.androidFastStart.ActivityBaseWithInitPermission
import io.github.nic562.androidFastStart.SomethingListable
import kotlinx.android.synthetic.main.activity_sms.*
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.*

/**
 * Created by Nic on 2019/12/20.
 */
class ActivitySMS : ActivityBaseWithInitPermission(), SomethingListable<SMS> {

    override val listableManager = object : SomethingListable.ListableManager<SMS>() {
        override val listItemLayoutID = R.layout.layout_sms_item

        override fun itemConvert(helper: SomethingListable.ViewHolder, item: SMS) {
            with(helper) {
                setText(R.id.tv_id, item.id)
                setText(R.id.tv_body, item.body)
                setText(R.id.tv_date, Date(item.date).toString())
                setText(R.id.tv_type, item.type)
                setText(R.id.tv_number, item.number)
            }
        }

        private val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE)

        override fun loadData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<SMS>) {
            val offset: Int = (page - 1) * limit
            val totalCount: Int
            var cur: Cursor? = null
            try {
                cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
                        ?: return
                totalCount = cur.count

                cur = contentResolver.query(
                        Telephony.Sms.CONTENT_URI,
                        projection,
                        null,
                        null,
                        "${Telephony.Sms.DATE} desc limit ${limit} offset ${offset}"
                ) ?: return
                var sms: SMS
                val smsList = arrayListOf<SMS>()

                while (cur.moveToNext()) {
                    sms = SMS()
                    with(sms) {
                        number = cur.getString(cur.getColumnIndex(Telephony.Sms.ADDRESS)) ?: ""
                        body = cur.getString(cur.getColumnIndex(Telephony.Sms.BODY)) ?: ""
                        id = cur.getString(cur.getColumnIndex(Telephony.Sms._ID))
                        date = cur.getLong(cur.getColumnIndex(Telephony.Sms.DATE))
                        type = if (cur.getInt(cur.getColumnIndex(Telephony.Sms.TYPE)) == 1) "接收" else "发送"
                    }
                    if (sms.number.isEmpty() or sms.body.isEmpty()) {
                        println("ignore::::\n" + sms.id + "\n" + sms.body + "\n" + sms.date + "?" + sms.type)
                        continue
                    }
                    smsList.add(sms)
                }
                dataCallback.onLoadData(smsList, totalCount)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cur?.close()
            }
        }
    }

    override val initPermissionsRunnable = object : RunnableWithPermissions {
        override val authFailedMsg = "读取短信需要这些权限"
        override val requestCode = 999
        override val permissions = arrayOf(
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS)

        override fun success() {
            toast("授权成功！")
            btn_read.isEnabled = true
        }
    }

    override fun getListableRecyclerView(): RecyclerView {
        return rv_sms
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        btn_read.isEnabled = false
        btn_read.setOnClickListener {
            listableManager.reloadData()
        }
        initListable()
    }

}