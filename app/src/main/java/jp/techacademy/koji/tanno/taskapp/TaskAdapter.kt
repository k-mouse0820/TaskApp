package jp.techacademy.koji.tanno.taskapp

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.content.Context
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context): BaseAdapter() {

    private val mLayoutInflater: LayoutInflater
    var mTaskList = mutableListOf<Task>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return mTaskList.size
    }

    override fun getItem(position: Int): Any {
        return mTaskList[position]
    }

    override fun getItemId(position: Int): Long {
        return mTaskList[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        textView1.text = mTaskList[position].title

        val mYear = mTaskList[position].year
        val mMonth = mTaskList[position].month
        val mDay = mTaskList[position].day
        val mHour = mTaskList[position].hour
        val mMinute = mTaskList[position].minute
        val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
        val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
        val timestamp = "$dateString $timeString"
        textView2.text = timestamp

        return view
    }
}