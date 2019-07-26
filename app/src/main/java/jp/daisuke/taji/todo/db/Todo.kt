package jp.daisuke.taji.todo.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Todo {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var text: String? = null

    var deletedAt: Date? = null
    var createdAt: Date? = null
}