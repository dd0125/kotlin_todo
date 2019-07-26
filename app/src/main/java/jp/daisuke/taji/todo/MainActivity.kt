package jp.daisuke.taji.todo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
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
                        editText.setText("")

                        // タスクを再読み込みする
                        refreshTasks()

                    }
                }
            }
            return@setOnEditorActionListener true
        }

        // タスクを再読み込みする
        refreshTasks()

    }
    data class TaskData(val name: String?)
    data class ViewHolder(val nameTextView: TextView)
    class TaskListAdapter(context: Context, taskList: List<TaskData>) : ArrayAdapter<TaskData>(context, 0, taskList) {
        private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = layoutInflater.inflate(R.layout.task_list_item, parent, false)
                val nameTextView:TextView = view.findViewById(R.id.name_text_view)
                holder = ViewHolder(
                    nameTextView = nameTextView
                )
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val task = getItem(position) as TaskData
            holder.nameTextView.text = task.name

            return view
        }
    }

    private fun refreshTasks() {
        val mainApplication = application as MainApplication
        val taskDao = mainApplication.getTaskDao()

        val activity = this
        var tasks:List<TaskData>? = null
        val launch = GlobalScope.launch {
            val taskList = taskDao!!.findAll()
            if(taskList == null){
                return@launch
            }
            tasks = List(taskList.size) { i ->

                TaskData(taskList[i].text)
            }

        }
        runBlocking{
            launch.join()
        }

        val adapter = TaskListAdapter(activity, tasks!!)
        task_list_view.adapter = adapter
    }

    /***
     * aaa
     */
    private fun insertTask(text:String):Boolean {

        val mainApplication = application as MainApplication
        val taskDao = mainApplication.getTaskDao()

        if(taskDao == null){
            return false
        }
        // todo の新規作成
        val task = Task()
        task.text = text
        taskDao.create(task)

        return true
    }



}
