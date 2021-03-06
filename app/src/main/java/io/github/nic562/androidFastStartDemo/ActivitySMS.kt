package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.selection.*
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.SlidingConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import io.github.nic562.androidFastStart.*
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

    private val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE)

    override val listableManager: ListableManager<Long> by lazy {
        instanceListableManager(R.layout.layout_item_sms, mutableListOf<SMS>())
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<SMS>) {
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
            dataCallback.onLoadData(smsList, totalCount, page)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cur?.close()
        }
    }

    override fun onCreateListableItemViewWrapper(parent: ViewGroup, viewType: Int): ViewGroup? {
        val v = LayoutInflater.from(this).inflate(R.layout.layout_item_sms, parent, false)
        val x = TextView(getOwnerContext()).apply {
            textSize = 14f
            setBackgroundColor(0xFFAA0000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            width = 200
        }
        return SmartSwipe.wrap(v).addConsumer(SlidingConsumer().apply {
            setRightDrawerView(x)
            releaseMode = SwipeConsumer.RELEASE_MODE_AUTO_CLOSE
            relativeMoveFactor = SlidingConsumer.FACTOR_FOLLOW
            setScrimColor(0x1F000000)
            addListener(object: SimpleSwipeListener(){
                override fun onSwipeStart(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {
                    val v = wrapper?.findViewById<TextView>(R.id.tv_body)
                    x.text = "swiped: ${v?.text} ${System.currentTimeMillis()}"
                }
            })
        }).wrapper
    }

    override fun listableItemConvert(helper: ViewHelper<Long>, item: SMS) {
        helper.apply {
            hSetText(R.id.tv_body, item.body)
            hSetText(R.id.tv_date, Date(item.date).toString())
            hSetText(R.id.tv_type, item.type)
            hSetText(R.id.tv_number, item.number)
            val ctvID: CheckedTextView = hGetView(R.id.tv_id)
            ctvID.text = item.id
            val details = helper.getItemDetails()
            val selectionTracker = listableManager.getSelectionTracker()
            if (details != null && selectionTracker != null) {
                ctvID.isChecked = selectionTracker.isSelected(details.selectionKey)
            } else {
                ctvID.isChecked = false
            }
        }
    }

    override fun getListableItemDetailsProvider(): ItemDetailsProvider<Long>? {
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

    private val loadSMSWithPermission = object : SomethingWithPermissions.RunnableWithPermissions {
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
        listableManager.apply {
            setFooterWithEmptyEnable(true)
            setHeaderWithEmptyEnable(true)
            setAnimationEnable(true)

            setSelectionStorageStrategy(StorageStrategy.createLongStorage())
            setSelectionKeyProvider(object : ItemKeyProvider<Long>(SCOPE_MAPPED) {
                override fun getKey(position: Int): Long {
                    return position.toLong()
                }

                override fun getPosition(key: Long): Int {
                    return key.toInt()
                }
            })

            initListable(rv_sms, withDefaultSelectionTracker = true)

            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
            setItemClickListener(object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    // 由于定义了itemViewWrapper 使用了 SmartSwipe生成的 SmartSwipeWrapper作为根节点 所以点击事件被拦截了？ 需使用子View 点击事件
                    toast("[click view]: >> position: $position")
                }
            })
            addChildClickViewIds(R.id.tv_id, R.id.tv_number)
            setItemChildClickListener(object : OnItemChildClickListener {
                override fun onItemChildClick(view: View, position: Int) {
                    toast("[click child view]: >>> position: $position")
                }
            })

            getSelectionTracker()?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    toast("selection size::: ${listableManager.getSelectionTracker()?.selection?.size()}")
                }
            })
        }
    }

}