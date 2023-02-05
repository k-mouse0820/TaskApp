package jp.techacademy.koji.tanno.taskapp

import java.io.Serializable
import java.util.Date
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


open class Task : RealmObject, Serializable {
    var title: String = ""
    var contents: String = ""
    var category: String = ""
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id:Int = 0

    /*constructor(title: String, contents: String) {
        this.title = title
        this.contents = contents
    }*/

}