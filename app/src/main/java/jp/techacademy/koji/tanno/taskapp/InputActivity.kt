package jp.techacademy.koji.tanno.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.renderscript.ScriptGroup.Input
import android.util.Log
import androidx.appcompat.widget.Toolbar
import android.view.View
import com.google.android.material.transition.MaterialSharedAxis
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import jp.techacademy.koji.tanno.taskapp.databinding.ActivityInputBinding
import jp.techacademy.koji.tanno.taskapp.databinding.ContentInputBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class InputActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityInputBinding
    private lateinit var contentBinding: ContentInputBinding
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format(
                    "%02d",
                    mMonth + 1
                ) + "/" + String.format("%02d", mDay)
                contentBinding.dateButton.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                contentBinding.timesButton.text = timeString
            }, mHour, mMinute, false
        )
    }

    private val mOnDoneClickListener = View.OnClickListener {
        CoroutineScope(Dispatchers.Default).launch {
            addTask()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        contentBinding = ContentInputBinding.inflate(layoutInflater)
        setContentView(contentBinding.root)

        // ActionBarを設定する
        val toolbar = activityBinding.toolbar as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        contentBinding.dateButton.setOnClickListener(mOnDateClickListener)
        contentBinding.timesButton.setOnClickListener(mOnTimeClickListener)
        contentBinding.doneButton.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val config = RealmConfiguration.Builder(schema = setOf(Task::class)).build()
        val realm = Realm.open(config)
        Log.d("Android","Start Query")
        mTask = realm.query<Task>("id == $0", taskId).first().find()
        Log.d("Android","End Query")
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            contentBinding.titleEditText.setText(mTask!!.title)
            contentBinding.contentEditText.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format(
                "%02d",
                mMonth + 1
            ) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            contentBinding.dateButton.text = dateString
            contentBinding.timesButton.text = timeString
        }
    }

    private suspend fun addTask() {
        val config = RealmConfiguration.Builder(schema = setOf(Task::class)).build()
        val realm = Realm.open(config)

        realm.write {

            if (mTask == null) {
                // 新規作成の場合
                mTask = Task()

//                val taskRealmMaxId = realm.query<Task>().max("id")
                val lastTask = realm.query<Task>().sort("id",Sort.DESCENDING).first().find()
                var identifier: Int = 0
                if (lastTask != null ) {
                    identifier = lastTask!!.id + 1
                }

                mTask!!.id = identifier
            }

            val title = contentBinding.titleEditText.text.toString()
            val content = contentBinding.contentEditText.text.toString()
            mTask!!.title = title
            mTask!!.contents = content

            val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
            val date = calendar.time
            mTask!!.date = date


        }
        realm.close()

    }



}