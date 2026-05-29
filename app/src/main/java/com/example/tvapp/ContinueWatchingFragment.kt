package com.example.tvapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvapp.data.WatchHistory
import kotlinx.coroutines.launch

class ContinueWatchingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_continue_watching, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.grid_view)
        val emptyView = view.findViewById<View>(R.id.empty_view)

        // 设置 GridLayout
        recyclerView.layoutManager = GridLayoutManager(context, 4)

        val activity = requireActivity()
        if (activity is MainActivity) {
            lifecycleScope.launch {
                val historyList = activity.database.historyDao().getContinueWatching()
                if (historyList.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = HistoryAdapter(historyList) { item ->
                        // 点击继续观看
                        val fragment = WebViewFragment()
                        fragment.arguments = Bundle().apply {
                            putString("url", item.videoUrl)
                            putString("category", item.title)
                        }
                        parentFragmentManager.commit {
                            replace(R.id.main_container, fragment)
                            addToBackStack("webview")
                        }
                    }
                }
            }
        }

        return view
    }
}

class HistoryAdapter(
    private val items: List<WatchHistory>,
    private val onClick: (WatchHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val progress: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        view.isFocusable = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        val pct = item.progressPercent
        holder.progress.text = "已观看 $pct%  ${formatTime(item.position)} / ${formatTime(item.duration)}"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}
