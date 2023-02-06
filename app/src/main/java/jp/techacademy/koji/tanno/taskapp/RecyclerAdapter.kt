package jp.techacademy.koji.tanno.taskapp

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort

class RecyclerAdapter: RecyclerView.Adapter<ViewHolderItem>() {

    val config =
        RealmConfiguration.Builder(schema = setOf(Task::class)).deleteRealmIfMigrationNeeded()
            .build()
    val mRealm = Realm.open(config)
    val rSortedTasks = mRealm.query<Task>().sort("id", Sort.DESCENDING).find()
    val taskList = mRealm.copyFromRealm(rSortedTasks) as MutableList<Task>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {

        val itemXml = LayoutInflater.from(parent.context)
            .inflate(R.layout.listitem_recyclerview1, parent, false)
        return ViewHolderItem(itemXml)
    }

    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        holder.titleHolder.text = taskList[position].title.toString()
        holder.contentHolder.text = taskList[position].contents.toString()
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

}