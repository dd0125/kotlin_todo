package jp.daisuke.taji.todo

import android.app.Application
import androidx.room.Room
import jp.daisuke.taji.todo.db.AppDataBase
import jp.daisuke.taji.todo.db.model.TaskDao


class MainApplication: Application() {
    private lateinit var taskDao: TaskDao;
    private val DATABASE_FILE_NAME = "todo.db";

    override fun onCreate() {
        super.onCreate()

        // TaskDao の準備
        setupTaskDao()
    }


    fun getTaskDao():TaskDao {
        return this.taskDao
    }


    private fun setupTaskDao() {

        val databaseBuilder = Room.databaseBuilder(applicationContext, AppDataBase::class.java, DATABASE_FILE_NAME)
        val database = databaseBuilder.build()
        this.taskDao = database.taskDao()

    }
}
