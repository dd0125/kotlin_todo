package jp.daisuke.taji.todo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Todo::class], version = 1, exportSchema = true)
@TypeConverters(DateConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}