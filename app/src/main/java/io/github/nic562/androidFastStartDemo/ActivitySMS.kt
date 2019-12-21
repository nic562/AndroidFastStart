package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingListable
import kotlinx.android.synthetic.main.activity_sms.*
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.*

/**
 * Created by Nic on 2019/12/20.
 */
class ActivitySMS : ActivityBase(), SomethingListable<SMS> {

    override val listableManager = object : SomethingListable.ListableManager<SMS>() {
        override val listItemLayoutID = R.layout.layout_sms_item

        override fun itemConvert(helper: SomethingListable.ViewHelper, item: SMS) {
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
                cur.close()

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

    private val loadSMSWithPermission = object : RunnableWithPermissions {
        override val authFailedMsg = "读取短信需要这些权限"
        override val requestCode = 999
        override val permissions = arrayOf(
                android.Manifest.permission.READ_SMS)

        override fun success() {
            toast("授权成功!")
            listableManager.reloadData()
        }

        override fun failed(deniedPermissions: List<String>) {
            toast("授权失败!")
        }
    }

    override fun getOwnerContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        btn_read.setOnClickListener {
            runWithPermissions(loadSMSWithPermission)
        }
        initListable(rv_sms)
        with(listableManager) {
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            setFooterWithEmptyEnable(true)
            setHeaderWithEmptyEnable(true)
            setAnimationEnable(true)
            setItemClickListener(object : SomethingListable.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    println("[click view]: ${view} >> position: ${position}")
                }
            })
            addChildClickViewIds(R.id.tv_id, R.id.tv_number)
            setItemChildClickListener(object : SomethingListable.OnItemChildClickListener {
                override fun onItemChildClick(view: View, position: Int) {
                    println("[click child view]: ${view} >>> position: ${position}")
                }
            })
        }
    }

}