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
import io.realm.kotlin.query.Sort


const val EXTRA_TASK = "jp.techacademy.koji.tanno.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mTaskAdapter: TaskAdapter
    private lateinit var mRealm: Realm
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.v("MAIN","ONCREATE-START")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val intent = Intent(this, InputActivity::class.java)

            Log.d("Android","Start intent")
            startActivity(intent)
            Log.d("Android","End intent")

        }

        // Realm の設定
        val config =
            RealmConfiguration.Builder(schema = setOf(Task::class)).deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.open(config)

        Log.v("REALM",mRealm.configuration.path)
        // C:\Users\tanno.koji\AppData\Local\Google\AndroidStudio2022.1\device-explorer\Nexus_5_API_24 [emulator-5554]\data\data\jp.techacademy.koji.tanno.taskapp\files

        // Listen for changes on whole collection
        val tasks: RealmQuery<Task> = mRealm.query(Task::class)

        // flow.collect() is blocking -- run it in a background context
        // この後使うcollectはスレッドをとめてしまうので、バックグラウンドで動かす

        Log.v("MAIN","CREATE-REALM-LISTENER-FLOW")
        val job = CoroutineScope(Dispatchers.Main).launch {
            // create a Flow from that collection, then add a listener to the Flow
            val tasksFlow =  tasks.asFlow()
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


        binding.searchButton.setOnClickListener {
            searchListView()
        }


        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)

        // ListViewをタップしたときの処理
        binding.listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に繊維させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        binding.listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val selectedTask = parent.adapter.getItem(position) as Task
            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(selectedTask.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                mRealm.writeBlocking {
                    val selectedTasks: RealmResults<Task> = query<Task>("id == $0", selectedTask.id).find()
                    delete(selectedTasks)
                }

                reloadListView()

            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()

    }

    private fun reloadListView() {
        val tasks = mRealm.query<Task>().sort("id", Sort.DESCENDING).find()
        Log.v("MAIN","ENDLIST")
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(tasks) as MutableList<Task>
        binding.listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

    }
    private fun searchListView() {
        Log.v("DEBUG",binding.searchTextBox.text.toString())
        val categoryTasks = mRealm.query<Task>("category == $0", binding.searchTextBox.text.toString()).sort("id",Sort.DESCENDING).find()
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(categoryTasks) as MutableList<Task>
        binding.listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }
    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        mRealm.close()
    }





}