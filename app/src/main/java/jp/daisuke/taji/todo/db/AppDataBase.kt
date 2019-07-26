package jp.daisuke.taji.todo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.daisuke.taji.todo.db.model.Task
import jp.daisuke.taji.todo.db.model.TaskDao

@Database(entities = [Task::class], version = 1, exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}