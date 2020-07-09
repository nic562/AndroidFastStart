package io.github.nic562.androidFastStartDemo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.palette.graphics.Palette
import com.google.android.material.appbar.AppBarLayout
import io.github.nic562.androidFastStart.ActivityBase
import io.github.nic562.androidFastStart.SomethingListable
import io.github.nic562.androidFastStart.viewholder.`interface`.ViewHelper
import kotlinx.android.synthetic.main.activity_coll.*
import kotlinx.android.synthetic.main.activity_coll.rv_cards
import kotlinx.android.synthetic.main.activity_coll.swipeRefreshLayout
import org.jetbrains.anko.longToast
import kotlin.math.abs
import kotlin.random.Random

/**
 * Created by Nic on 2020/7/9.
 */
class ActivityColl : ActivityBase(), SomethingListable<String, Long> {

    private var appBarExpanded = false
    private var collMenu: Menu? = null
    private var headImageColorPrimary: Int? = null
    private var headImageColorPrimaryVibrant: Int? = null

    override val listableManager: SomethingListable.DataListableManager<String, Long> by lazy {
        instanceListableManager(R.layout.layout_item_card, mutableListOf<String>())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coll)
        setSupportActionBar(toolbar)

        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _: AppBarLayout, verticalOffset: Int ->
            val t = abs(verticalOffset) < 200
            if (t != appBarExpanded) {
                appBarExpanded = t
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (appBarExpanded) {
                        window.statusBarColor = Color.TRANSPARENT
                    } else {
                        headImageColorPrimaryVibrant?.let {
                            window.statusBarColor = it
                        }
                    }
                }
                invalidateOptionsMenu()
            }
        })

        checkHeaderColor()

        listableManager.apply {
            initListable(rv_cards)
            setEmptyView(R.layout.layout_list_empty)
        }
        swipeRefreshLayout.setOnRefreshListener {
            loadDelay()
        }
    }

    override fun onStart() {
        super.onStart()
        loadDelay()
    }

    private fun checkHeaderColor() {
        val bm = (head_img.drawable as BitmapDrawable).bitmap
        Palette.from(bm).generate {
            it?.apply {
                val mColor = getMutedColor(R.attr.colorPrimary)
                collapsingToolbar.setContentScrimColor(mColor)
                headImageColorPrimary = mColor
                headImageColorPrimaryVibrant = getDarkMutedColor(mColor)
            }
        }
    }

    private fun loadDelay() {
        listableManager.setCanLoadMore(false) // 防止触发下拉加载数据事件
        swipeRefreshLayout.isRefreshing = true
        rv_cards.postDelayed({
            listableManager.reloadData()
        }, 2000)
    }

    override fun loadListableData(page: Int, limit: Int, dataCallback: SomethingListable.OnLoadDataCallback<String>) {
        swipeRefreshLayout.isRefreshing = false
        listableManager.setCanLoadMore(true)
        if (Random(System.currentTimeMillis()).nextInt() % 3 == 0) {
            longToast("测试错误！！~~")
            println("page: $page 测试错误！！~~")
            dataCallback.onError()
            return
        }
        val l = arrayListOf<String>()
        for (i in 1..limit) {
            l.add("$page ${page * limit + i}")
        }
        dataCallback.onLoadData(l, 50, page)
    }

    override fun listableItemConvert(helper: ViewHelper<Long>, item: String) {
        helper.apply {
            hSetText(R.id.tv_title, item)
            hSetText(R.id.tv_subtitle, item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dragged, menu)
        collMenu = menu
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (collMenu != null && (!appBarExpanded || collMenu?.size() != 1)) {
            collMenu?.add("Mail")?.setIcon(android.R.drawable.ic_dialog_email)?.setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM
            )
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun getOwnerContext(): Context {
        return this
    }
}