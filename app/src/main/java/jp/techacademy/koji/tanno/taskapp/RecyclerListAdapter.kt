package jp.techacademy.koji.tanno.taskapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.techacademy.koji.tanno.taskapp.databinding.ListitemRecyclerview1Binding



class RecyclerListAdapter(val listData: MutableList<Task>): RecyclerView.Adapter<RecyclerListAdapter.RecyclerListViewHolder>() {


    class RecyclerListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var listItemRecyclerview1Binding = ListitemRecyclerview1Binding.inflate(layoutInflater)
        setContentView(listItemRecyclerview1Binding.root)


        val titleText = view.findViewById<String>(R.id.titleText)
        val contentText = view.
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val item = inflater.inflate(R.layout.listitem_recyclerview1, parent, false)
        return RecyclerListViewHolder(item)
    }

    // RecyclerViewがアダプタからビューホルダを受け取った際にRecyclerViewから呼び出される処理
    override fun onBindViewHolder(holder: RecyclerListViewHolder, position: Int) {

        // ビューホルダが保持するビューに、データを反映
        val task = _listData[position]
        holder._titleRow.text = task.title
        holder._contentRow.text = task.contents
    }

    override fun getItemCount(): Int {
        return _listData.size
    }
}

