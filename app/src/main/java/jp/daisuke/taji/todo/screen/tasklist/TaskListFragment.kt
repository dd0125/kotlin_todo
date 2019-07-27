package jp.daisuke.taji.todo.screen.tasklist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import jp.daisuke.taji.todo.MainApplication
import jp.daisuke.taji.todo.R
import jp.daisuke.taji.todo.db.model.Task
import kotlinx.android.synthetic.main.fragment_task_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking





class TaskListFragment : Fragment() {
    private fun getMainApplication() :MainApplication {
        return activity?.application as MainApplication
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNewTodoEditText = view.findViewById<EditText>(R.id.input_new_todo_edit_text)

        inputNewTodoEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val editText: EditText = v as EditText
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
                        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

                        // タスクを再読み込みする
                        reloadTaskList()

                    }
                }
            }
            return@setOnEditorActionListener true
        }

        // TaskListView を再読み込みする
        reloadTaskList()

    }


    private fun reloadTaskList() {
        val mainApplication = activity?.application as MainApplication
        val taskDao = mainApplication.getTaskDao()

//        val activity = this

        runBlocking{
            val deferred = GlobalScope.async {
                val taskList = taskDao.findAll()
                return@async taskList
            }
            val taskList:List<Task>? = deferred.await()
            val adapter = TaskListAdapter(activity!!, taskList!!)
            adapter.setUpdateTaskFunction {
                val task = it

                val mainApplication = getMainApplication() as MainApplication
                val taskDao = mainApplication.getTaskDao()

                runBlocking {
                    val l = GlobalScope.launch {
                        taskDao.update(task)
                    }
                    l.join()
                }
                // TaskListView を再読み込み
                reloadTaskList()

            }


            task_list_view.adapter = adapter
        }

    }

    /***
     * aaa
     */
    private fun insertTask(name:String):Boolean {

        val mainApplication = activity?.application as MainApplication
        val taskDao = mainApplication.getTaskDao()

        // task の新規作成
        val task = Task()
        task.name = name
        taskDao.create(task)

        return true
    }



}
