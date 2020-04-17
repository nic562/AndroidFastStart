package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingListable
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_page.*

/**
 * Created by Nic on 2020/4/17.
 */
class ActivityPage : ActivityBase(), SomethingListable<String, Long> {
    private val dataList = mutableListOf<String>()

    override val listableManager: SomethingListable.DataListableManager<String, Long> by lazy {
        instanceListableManager(R.layout.layout_item_card, dataList)
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<String>) {
        println("loading $page ...")
        val ss = arrayListOf<String>()
        for (i in 0..limit) {
            ss.add("test $page - $i")
        }
        dataCallback.onLoadData(ss, 50, page)
        tv_page_info.text = "第${page}页"
    }

    override fun listableItemConvert(helper: ViewHelper<Long>, item: String) {
        helper.apply {
            hSetText(R.id.tv_title, item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)
        btn_submit.setOnClickListener {
            val p = et_page.text.toString()
            if (p.isEmpty()) {
                return@setOnClickListener
            }
            listableManager.clearData()
            listableManager.loadData(p.toInt())
        }
        initListable(rv_list)
    }

    override fun onStart() {
        super.onStart()
        listableManager.reloadData()
    }

    override fun getOwnerContext(): Context {
        return this
    }
}