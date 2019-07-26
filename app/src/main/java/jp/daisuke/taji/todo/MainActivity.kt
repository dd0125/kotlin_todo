package jp.daisuke.taji.todo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import jp.daisuke.taji.todo.db.AppDataBase
import jp.daisuke.taji.todo.db.Todo
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val databaseFileName = "kotlin_room_sample.db"

//        Room.databaseBuilder(this, DataBase::class.java, databaseFileName)
        val databaseBuilder = Room.databaseBuilder(this, AppDataBase::class.java, databaseFileName)
//        Log.d("data",Integer.toString( databaseBuilder))

        val database = databaseBuilder.build()

        val todo = Todo()
        todo.text = "こんばんは"

        val todoDao = database.todoDao()
        thread {
            todoDao.create(todo)
        }

        thread{
            val todoList = todoDao.findAll()
            Log.d("data","Size = " + Integer.toString( todoList.size))

            todoList.forEach {
                Log.d("data",it.text)
            }
        }



    }
}
