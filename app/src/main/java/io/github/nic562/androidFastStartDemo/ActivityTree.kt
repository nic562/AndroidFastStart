package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.os.Bundle
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.ListableManager
import io.github.nic562.androidFastStart.SomethingTreeListable
import io.github.nic562.androidFastStart.viewholder.`interface`.TreeAble
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_card.*

/**
 * Created by Nic on 2020/2/22.
 */
class ActivityTree : ActivityBase(), SomethingTreeListable<Long> {
    override val listableManager: ListableManager<Long> by lazy {
        instanceListableManager()
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingTreeListable.OnLoadDataCallback) {
        val data = arrayListOf<TreeAble>()
        for (i in 0 until limit) {
            val ch = arrayListOf<TreeAble>()
            for (j in 0 until limit) {
                ch.add(object : TreeAble {
                    override val tree: Int = 1
                    override val layoutResID: Int = R.layout.layout_tree_child
                    override val children: MutableList<TreeAble>? = null

                    private val title = "child: $i $j"

                    override fun <K> convert(helper: ViewHelper<K>) {
                        helper.hSetText(R.id.tv_tree_child, title)
                    }
                })
            }
            data.add(object : TreeAble {
                override val tree: Int = 0
                override val layoutResID: Int = R.layout.layout_tree_root
                override val children: MutableList<TreeAble>? = ch

                private val title = "root: $i"

                override fun <K> convert(helper: ViewHelper<K>) {
                    helper.hSetText(R.id.tv_tree_root, title)
                }
            })
        }

        dataCallback.onLoadData(data, 20)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        with(listableManager) {
            setAnimationEnable(true)
            setHeaderWithEmptyEnable(true)
            setFooterWithEmptyEnable(true)

            initListable(rv_cards)
            setEmptyView(R.layout.layout_list_empty)
            addHeaderView(R.layout.layout_header)
            addFooterView(R.layout.layout_footer)
        }
    }

    override fun onStart() {
        super.onStart()
        listableManager.reloadData()
    }

    override fun getOwnerContext(): Context {
        return this
    }
}