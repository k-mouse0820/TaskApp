package jp.techacademy.koji.tanno.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


open class Task : RealmObject, Serializable {
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id:Int = 0

    /*constructor(title: String, contents: String) {
        this.title = title
        this.contents = contents
    }*/
}