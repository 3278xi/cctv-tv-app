package com.example.tvapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchInput = view.findViewById<TextView>(R.id.search_input)
        val searchBtn = view.findViewById<Button>(R.id.btn_do_search)

        searchBtn.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                openSearch(query)
            }
        }

        // 热门搜索关键词
        val hotKeywords = listOf("熊出没", "小猪佩奇", "汪汪队", "西游记", "哪吒",
            "大头儿子", "超级飞侠", "喜羊羊", "萌鸡小队", "螺丝钉")

        val hotContainer = view.findViewById<ViewGroup>(R.id.hot_container)
        hotKeywords.forEach { keyword ->
            val btn = Button(requireContext()).apply {
                text = keyword
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.white, null))
                setBackgroundResource(R.drawable.nav_button_bg)
                isFocusable = true
                setPadding(24, 12, 24, 12)
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 8, 8, 8) }
                setOnClickListener {
                    searchInput.text = keyword
                    openSearch(keyword)
                }
            }
            hotContainer.addView(btn)
        }

        return view
    }

    private fun openSearch(query: String) {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "https://tv.cctv.com/search/?keyword=$encoded"
        val fragment = WebViewFragment()
        fragment.arguments = Bundle().apply {
            putString("url", url)
            putString("category", "搜索: $query")
        }
        parentFragmentManager.commit {
            replace(R.id.main_container, fragment)
            addToBackStack("search_result")
        }
    }
}
