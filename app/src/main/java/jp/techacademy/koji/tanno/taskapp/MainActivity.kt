package jp.techacademy.koji.tanno.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.koji.tanno.taskapp.databinding.ActivityMainBinding
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.kotlin.query.Sort
import java.util.*


const val EXTRA_TASK = "jp.techacademy.koji.tanno.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mRealm: Realm
    private lateinit var job: Job
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTaskAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.v("MAIN", "ONCREATE-START")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val intent = Intent(this, InputActivity::class.java)

            Log.d("Android", "Start intent")
            startActivity(intent)
            Log.d("Android", "End intent")

        }

        // Realm の設定
        val config =
            RealmConfiguration.Builder(schema = setOf(Task::class)).deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.open(config)

        Log.v("REALM", mRealm.configuration.path)
        // C:\Users\tanno.koji\AppData\Local\Google\AndroidStudio2022.1\device-explorer\Nexus_5_API_24 [emulator-5554]\data\data\jp.techacademy.koji.tanno.taskapp\files

        // Listen for changes on whole collection
        val rTasks: RealmQuery<Task> = mRealm.query(Task::class)

        // flow.collect() is blocking -- run it in a background context
        // この後使うcollectはスレッドをとめてしまうので、バックグラウンドで動かす

        Log.v("MAIN", "CREATE-REALM-LISTENER-FLOW")
        val job = CoroutineScope(Dispatchers.Main).launch {
//        val job = GlobalScope.launch(Dispatchers.Main) {
            // create a Flow from that collection, then add a listener to the Flow
            val tasksFlow = rTasks.asFlow()
            val subscription = tasksFlow.collect { changes: ResultsChange<Task> ->
                when (changes) {
                    // UpdatedResults means this change represents an update/insert/delete operation
                    is UpdatedResults -> {
                        reloadListView()
                    }
                    else -> {
                        // types other than UpdatedResults are not changes -- ignore them
                    }
                }
            }
        }


        // RecyclerViewの設定
        mRecyclerView = binding.recyclerview1
        mRecyclerView.adapter = RecyclerAdapter()
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        // 区切り線を表示するDividerItemDecorationオブジェクト
        val decorator = DividerItemDecoration(this, LinearLayoutManager(this).orientation)
        mRecyclerView.addItemDecoration(decorator)

        /*

        mTaskAdapter.setOnItemClickListener(object:RecyclerListAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, clickedTask: Task) {
                val intent = Intent(this@MainActivity, InputActivity::class.java)
                intent.putExtra(EXTRA_TASK, clickedTask.id)
                startActivity(intent)
            }
        })
*/
/*        // ListViewをタップしたときの処理
        fun onItemClick(view: View, position: Int) {
            val task = mRecyclerView.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }



        // ListViewを長押ししたときの処理
        binding.recyclerview1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val selectedTask = parent.adapter.getItem(position) as Task
            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(selectedTask.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                mRealm.writeBlocking {
                    val selectedTasks: RealmResults<Task> =
                        query<Task>("id == $0", selectedTask.id).find()
                    delete(selectedTasks)
                }

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    selectedTask.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)


                reloadListView()

            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
*/


        reloadListView()

    }

    private fun reloadListView() {
        val mTaskAdapter = RecyclerAdapter()
        mRecyclerView.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        mRealm.close()
    }

}


/*
private class RecyclerListAdapter(listData: MutableList<Task>): RecyclerView.Adapter<RecyclerListViewHolder>() {

    private val recyclerListData = listData
    lateinit var clickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.listitem_recyclerview1, parent, false)

        // インフレートしたアイテムビューをリスナとしてセット
//            view.setOnClickListener(ItemClickListener())
//            view.setOnLongClickListener(ItemLongClickListener())

        // ビューホルダの作成、返却
        return RecyclerListViewHolder(view)
    }

    // RecyclerViewがアダプタからビューホルダを受け取った際にRecyclerViewから呼び出される処理
    override fun onBindViewHolder(holder: RecyclerListViewHolder, position: Int) {

        // ビューホルダが保持するビューに、データを反映
        //holder.taskId = recyclerListData[position].id
        holder.titleRow.text = recyclerListData[position].title
        holder.contentRow.text = recyclerListData[position].contents

    }
    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int, clickedTask: Task)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    override fun getItemCount(): Int {
        return recyclerListData.size
    }
}

private class RecyclerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var titleRow: TextView
    var contentRow: TextView

    init {
        titleRow = itemView.findViewById<TextView>(R.id.titleText)
        contentRow = itemView.findViewById<TextView>(R.id.contentText)
    }
}
*/