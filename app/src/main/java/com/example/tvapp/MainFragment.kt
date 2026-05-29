package com.example.tvapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.tvapp.data.AppDatabase

class MainFragment : Fragment() {

    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = (requireActivity() as MainActivity).database

        // 导航按钮点击事件
        view.findViewById<View>(R.id.btn_cartoon).setOnClickListener {
            openWebView("https://tv.cctv.com/yxg/index.shtml?spm=C96370.PPDB2vhvSivD.EFGWjAl3vmeC.2#datacid=dhp&datafl=&datadq=&fc=%E5%8A%A8%E7%94%BB%E7%89%87&dataszm=", "动画片")
        }
        view.findViewById<View>(R.id.btn_tv_series).setOnClickListener {
            openWebView("https://tv.cctv.com/yxg/index.shtml?spm=C96370.PPDB2vhvSivD.EFGWjAl3vmeC.2#datacid=dsj&datafl=&datadq=&fc=%E7%94%B5%E8%A7%86%E5%89%A7&dataszm=", "电视剧")
        }
        view.findViewById<View>(R.id.btn_documentary).setOnClickListener {
            openWebView("https://tv.cctv.com/yxg/index.shtml?spm=C96370.PPDB2vhvSivD.EFGWjAl3vmeC.2#datacid=jlp&datafl=&datadq=&fc=%E7%BA%AA%E5%BD%95%E7%89%87&dataszm=", "纪录片")
        }
        view.findViewById<View>(R.id.btn_continue).setOnClickListener {
            openContinueWatching()
        }
        view.findViewById<View>(R.id.btn_search).setOnClickListener {
            openSearch()
        }

        // 更新继续观看数量
        updateContinueCount(view)
    }

    private fun openWebView(url: String, category: String) {
        val fragment = WebViewFragment()
        fragment.arguments = Bundle().apply {
            putString("url", url)
            putString("category", category)
        }
        parentFragmentManager.commit {
            replace(R.id.main_container, fragment)
            addToBackStack("webview")
        }
    }

    private fun openContinueWatching() {
        parentFragmentManager.commit {
            replace(R.id.main_container, ContinueWatchingFragment())
            addToBackStack("continue")
        }
    }

    private fun openSearch() {
        parentFragmentManager.commit {
            replace(R.id.main_container, SearchFragment())
            addToBackStack("search")
        }
    }

    private fun updateContinueCount(view: View) {
        val countView = view.findViewById<TextView>(R.id.continue_count)
        androidx.lifecycle.lifecycleScope.launchWhenStarted {
            try {
                val count = database.historyDao().getContinueWatching().size
                countView.text = if (count > 0) "$count 个" else ""
            } catch (_: Exception) {}
        }
    }
}
