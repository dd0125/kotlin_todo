package jp.daisuke.taji.todo.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Task {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var name: String? = null

    var doneAt: Date? = null
    var createdAt: Date? = null
}