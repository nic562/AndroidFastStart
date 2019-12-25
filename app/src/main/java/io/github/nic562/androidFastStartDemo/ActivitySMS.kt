package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.view.MotionEvent
import android.view.View
import android.widget.CheckedTextView
import androidx.recyclerview.selection.*
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingListable
import io.github.nic562.androidFastStart.viewholder.ItemDetails
import io.github.nic562.androidFastStart.viewholder.`interface`.ItemDetailsProvider
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_sms.*
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.Date

/**
 * Created by Nic on 2019/12/20.
 */
class ActivitySMS : ActivityBase(), SomethingListable<SMS, Long> {

    private val selectionTracker by lazy {
        SelectionTracker.Builder<Long>(
                "my_selection",
                rv_sms,
                object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
                    override fun getKey(position: Int): Long {
                        return position.toLong()
                    }

                    override fun getPosition(key: Long): Int {
                        return key.toInt()
                    }
                },
                object : ItemDetailsLookup<Long>() {
                    @Suppress("UNCHECKED_CAST")
                    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
                        println("??????? get item details!!! ${e.action}")
                        val v = rv_sms.findChildViewUnder(e.x, e.y)
                        if (v != null) {
                            val viewHolder = rv_sms.getChildViewHolder(v)
                            return (viewHolder as ViewHelper<Long>).getItemDetails()
                        }
                        return null
                    }
                },
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything<Long>()).build()
    }

    override val listableManager = object : SomethingListable.ListableManager<SMS, Long>() {
        override val listItemLayoutID = R.layout.layout_item_sms

        override fun itemConvert(helper: ViewHelper<Long>, item: SMS) {
            with(helper) {
                hSetText(R.id.tv_body, item.body)
                hSetText(R.id.tv_date, Date(item.date).toString())
                hSetText(R.id.tv_type, item.type)
                hSetText(R.id.tv_number, item.number)
                val ctvID: CheckedTextView = hGetView(R.id.tv_id)
                ctvID.text = item.id
                val details = helper.getItemDetails()
                if (details != null) {
                    ctvID.isChecked = selectionTracker.isSelected(details.selectionKey)
                } else {
                    ctvID.isChecked = false
                }
            }
        }

        override fun getItemDetailsProvider(): ItemDetailsProvider<Long>? {
            return object : ItemDetailsProvider<Long> {
                override fun create(): ItemDetails<Long> {
                    return object : ItemDetails<Long>() {
                        override fun getSelectionKey(): Long? {
                            return position.toLong()
                        }
                    }
                }
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
                        "${Telephony.Sms.DATE} desc limit $limit offset $offset"
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
        selectionTracker.addObserver(object: SelectionTracker.SelectionObserver<Long>(){
            override fun onSelectionChanged() {
                println("selection size::: ${selectionTracker.selection.size()}")
            }
        })
        with(listableManager) {
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            setFooterWithEmptyEnable(true)
            setHeaderWithEmptyEnable(true)
            setAnimationEnable(true)
            setItemClickListener(object : SomethingListable.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    println("[click view]: $view >> position: $position")
                }
            })
            addChildClickViewIds(R.id.tv_id, R.id.tv_number)
            setItemChildClickListener(object : SomethingListable.OnItemChildClickListener {
                override fun onItemChildClick(view: View, position: Int) {
                    println("[click child view]: $view >>> position: $position")
                }
            })
        }
    }

}