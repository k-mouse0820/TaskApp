package jp.techacademy.koji.tanno.taskapp

import android.app.AlarmManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
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
    private var mTaskId = -1
    private var mRealm: Realm? = null

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
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()

        finish()
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
        mTaskId = intent.getIntExtra(EXTRA_TASK, -1)
        val config = RealmConfiguration.Builder(schema = setOf(Task::class)).deleteRealmIfMigrationNeeded().build()
        mRealm = Realm.open(config)

        var rTask: Task? = null

        rTask = mRealm!!.query<Task>("id = $0", mTaskId).first().find()
        Log.d("DEBUG", rTask.toString())
        Log.d("DEBUG", rTask?.id.toString())
        Log.d("DEBUG", rTask?.title.toString())
        Log.d("DEBUG", rTask?.contents.toString())
        Log.d("DEBUG", rTask?.category.toString())
        Log.d("DEBUG", rTask?.year.toString())
        Log.d("DEBUG", rTask?.month.toString())
        Log.d("DEBUG", rTask?.day.toString())
        Log.d("DEBUG", rTask?.hour.toString())
        Log.d("DEBUG", rTask?.minute.toString())

        if (rTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            contentBinding.titleEditText.setText(rTask!!.title)
            contentBinding.contentEditText.setText(rTask!!.contents)
            contentBinding.categoryEditText.setText(rTask!!.category)
            mYear = rTask!!.year
            mMonth = rTask!!.month
            mDay = rTask!!.day
            mHour = rTask!!.hour
            mMinute = rTask!!.minute

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            contentBinding.dateButton.text = dateString
            contentBinding.timesButton.text = timeString
        }
    }

    private fun addTask() {

        val editTitle = contentBinding.titleEditText.text.toString()
        val editContent = contentBinding.contentEditText.text.toString()
        val editCategory = contentBinding.categoryEditText.text.toString()
        var rTask: Task? = null
        var identifier: Int = 0

        mRealm!!.writeBlocking {

            rTask = mRealm!!.query<Task>("id = $0", mTaskId).first().find()

            Log.v("INPUT", String.format("%02d",rTask?.hour) + ":" + String.format("%02d", rTask?.minute))

            if (rTask != null) {
                Log.v("INPUT","UPDATE-START")
                findLatest(rTask!!)?.title = editTitle
                findLatest(rTask!!)?.contents = editContent
                findLatest(rTask!!)?.category = editCategory
                findLatest(rTask!!)?.year = mYear
                findLatest(rTask!!)?.month = mMonth
                findLatest(rTask!!)?.day = mDay
                findLatest(rTask!!)?.hour = mHour
                findLatest(rTask!!)?.minute = mMinute
                identifier = findLatest(rTask!!)?.id!!
                Log.v("INPUT","UPDATE-END")
            } else {
                // 新規作成の場合
                Log.v("INPUT","INSERT-START")
                val lastTask = mRealm!!.query<Task>().sort("id", Sort.DESCENDING).first().find()
                if (lastTask != null) {
                    identifier = lastTask!!.id + 1
                }
                this.copyToRealm(Task().apply {
                    id = identifier
                    title = editTitle
                    contents = editContent
                    category = editCategory
                    year = mYear
                    month = mMonth
                    day = mDay
                    hour = mHour
                    minute = mMinute
                })
            }
        }

        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, identifier)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            identifier,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm!!.close()
    }

}