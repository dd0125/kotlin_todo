package jp.daisuke.taji.todo

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import jp.daisuke.taji.todo.db.model.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking




class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        input_new_todo_edit_text.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val editText:EditText = v as EditText
                val text:String = editText.text.toString()
                val deferred = GlobalScope.async {
                    // タスクを新規保存
                    val isSuccess = insertTask(text)

                    return@async isSuccess
                }
                runBlocking{
                    val isSuccess = deferred.await()
                    if(isSuccess){
                        // 入力値をクリアする
                        editText.text.clear()

                        // キーボードを閉じる
                        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

                        // タスクを再読み込みする
                        reloadTaskListView()

                    }
                }
            }
            return@setOnEditorActionListener true
        }

        // TaskListView を再読み込みする
        reloadTaskListView()

    }
//    private data class TaskData(val name: String?)

    private fun reloadTaskListView() {
        val mainApplication = application as MainApplication
        val taskDao = mainApplication.getTaskDao()

        val activity = this

        runBlocking{
            val deferred = GlobalScope.async {
                val taskList = taskDao.findAll()
                return@async taskList
            }
            val taskList:List<Task>? = deferred.await()
            val adapter = TaskListAdapter(activity, taskList!!)
            adapter.setUpdateTaskFunction {
                val task = it

                val mainApplication = applicationContext as MainApplication
                val taskDao = mainApplication.getTaskDao()

                runBlocking {
                    val l = GlobalScope.launch {
                        taskDao.update(task)
                    }
                    l.join()
                }
                // TaskListView を再読み込み
                reloadTaskListView()

            }


            task_list_view.adapter = adapter
        }

    }

    /***
     * aaa
     */
    private fun insertTask(name:String):Boolean {

        val mainApplication = application as MainApplication
        val taskDao = mainApplication.getTaskDao()

        // task の新規作成
        val task = Task()
        task.name = name
        taskDao.create(task)

        return true
    }



}
