package jp.techacademy.koji.tanno.taskapp

import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView


class ViewHolderItem(itemView: View): RecyclerView.ViewHolder(itemView) {

    val titleHolder : TextView = itemView.findViewById(R.id.titleText)
    val contentHolder: TextView = itemView.findViewById(R.id.contentText)

    private val recyclerAdapter = RecyclerAdapter()
    private val taskList = recyclerAdapter.taskList

    init {
        itemView.setOnClickListener {
            val position:Int = bindingAdapterPosition
            Toast.makeText(itemView.context, "${taskList[position].title}さんです", Toast.LENGTH_SHORT).show()
            val intent = Intent(itemView.context, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, taskList[position].id)
            startActivity(itemView.context, intent, null)
        }
    }


}