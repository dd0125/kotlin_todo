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
import java.util.*


class TaskListFragment : Fragment() {
    private fun getMainApplication() :MainApplication {
        return activity?.application as MainApplication
    }
    var currentTaskList: List<Task>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNewTodoEditText = view.findViewById<EditText>(R.id.input_new_todo_edit_text)

        // タスクの追加
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

        // 全てのタスクを完了にする
        all_complate_button.setOnClickListener {
            if(currentTaskList == null){
                return@setOnClickListener
            }
            val mainApplication = getMainApplication() as MainApplication
            val taskDao = mainApplication.getTaskDao()

            runBlocking {
                val launch = GlobalScope.launch {
                    currentTaskList?.forEach {
                        val task = it
                        if(task.doneAt == null){
                            task.doneAt = Date()
                            // タスクの更新
                            taskDao.update(task)
                        }
                    }
                }
                launch.join()

                // タスクを再読み込みする
                reloadTaskList()

            }
        }

        // 完了済のタスクを削除する
        clear_complated_button.setOnClickListener {
            if(currentTaskList == null){
                return@setOnClickListener
            }
            val mainApplication = getMainApplication() as MainApplication
            val taskDao = mainApplication.getTaskDao()

            runBlocking {
                val launch = GlobalScope.launch {
                    currentTaskList?.forEach {
                        val task = it
                        if(task.doneAt != null){
                            // タスクの削除
                            taskDao.delete(task)
                        }
                    }
                }
                launch.join()

                // タスクを再読み込みする
                reloadTaskList()

            }
        }

        // TaskListView を再読み込みする
        reloadTaskList()

    }


    private fun reloadTaskList() {
        val mainApplication = getMainApplication() as MainApplication
        val taskDao = mainApplication.getTaskDao()

//        val activity = this

        runBlocking{
            val deferred = GlobalScope.async {
                val taskList = taskDao.findAll()
                return@async taskList
            }
            val taskList:List<Task>? = deferred.await()
            currentTaskList = taskList

            // ListAdapter の設定
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
            adapter.setDeleteTaskFunction {
                val task = it

                val mainApplication = getMainApplication() as MainApplication
                val taskDao = mainApplication.getTaskDao()

                runBlocking {
                    val l = GlobalScope.launch {
                        taskDao.delete(task)
                    }
                    l.join()
                }
                // TaskListView を再読み込み
                reloadTaskList()

            }
            task_list_view.adapter = adapter

            var activeTaskCount = 0
            taskList.forEach {
                if(it.doneAt == null){
                    // 完了日時が入っていないタスクのみカウントする
                    activeTaskCount++
                }
            }

            active_item_count_view.text = activeTaskCount.toString() + " items left"

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