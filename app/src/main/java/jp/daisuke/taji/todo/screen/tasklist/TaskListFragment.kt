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
    enum class TaskDisplayState {
        All, Active, Complete
    }
    var taskDisplayState: TaskDisplayState = TaskListFragment.TaskDisplayState.All

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(jp.daisuke.taji.todo.R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNewTodoEditText = view.findViewById<EditText>(jp.daisuke.taji.todo.R.id.input_new_todo_edit_text)

        // タスクの追加
        inputNewTodoEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val editText: EditText = v as EditText
                val text:String = editText.text.toString()

                if(text.isEmpty()){
                    // 入力文字がない場合は処理しない
                    return@setOnEditorActionListener true
                }

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


                        // タスクを再読み込みする
                        reloadTaskList()

                    }
                }
            }
            return@setOnEditorActionListener true
        }
        // タスク新規登録時の入力欄からフォーカスが外れた場合、キーボードを閉じる
        inputNewTodoEditText.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                // キーボードを閉じる
                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            }
        }

        // 全てのタスクを完了にする。既に全て完了の場合は、全て未完了にする
        all_complete_switch.setOnClickListener {
            if(currentTaskList == null){
                return@setOnClickListener
            }
            val mainApplication = getMainApplication() as MainApplication
            val taskDao = mainApplication.getTaskDao()

            runBlocking {
                val launch = GlobalScope.launch {
                    var completeTaskCount = 0
                    currentTaskList!!.forEach {
                        if(it.doneAt != null){
                            completeTaskCount++
                        }
                    }
                    val taskCount = currentTaskList!!.size

                    if(taskCount == completeTaskCount){
                        // 既に全てが完了ならば、全て未完了に更新する
                        currentTaskList?.forEach {
                            val task = it
                            if(task.doneAt != null){
                                task.doneAt = null
                                // タスクの更新
                                taskDao.update(task)
                            }
                        }
                    }else{
                        // 全て完了に更新する
                        currentTaskList?.forEach {
                            val task = it
                            if(task.doneAt == null){
                                task.doneAt = Date()
                                // タスクの更新
                                taskDao.update(task)
                            }
                        }
                    }
                }
                launch.join()

                // タスクを再読み込みする
                reloadTaskList()

            }
        }

        // タスクの表示条件選択
        radio_group_task_state.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radio_button_all -> {
                    taskDisplayState = TaskDisplayState.All
                }
                R.id.radio_button_active -> {
                    taskDisplayState = TaskDisplayState.Active
                }
                R.id.radio_button_complete -> {
                    taskDisplayState = TaskDisplayState.Complete
                }

            }
            // タスクを再読み込みする
            reloadTaskList()
        }

        // 完了済のタスクを削除する
        clear_completed_button.setOnClickListener {
            if(currentTaskList == null){
                return@setOnClickListener
            }
            val mainApplication = getMainApplication()
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

        radio_button_all.isChecked = true

        // TaskListView を再読み込みする
        reloadTaskList()

    }


    private fun reloadTaskList() {
        val mainApplication = getMainApplication() as MainApplication
        val taskDao = mainApplication.getTaskDao()

        runBlocking{
            val deferred = GlobalScope.async {
                val taskList = when(taskDisplayState){
                    TaskDisplayState.All -> taskDao.findAll()
                    TaskDisplayState.Active -> taskDao.findActive()
                    TaskDisplayState.Complete -> taskDao.findComplete()
                }
                return@async taskList
            }
            val taskList:List<Task>? = deferred.await()
            currentTaskList = taskList

            // ListAdapter の設定
            if(task_list_view.adapter != null){
                val adapter = task_list_view.adapter as TaskListAdapter
                adapter.clear()
                adapter.addAll(taskList)
                adapter.notifyDataSetChanged()
            }else{
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

            }

            val taskListSize:Int
            var activeTaskCount = 0
            if(taskList != null){
                taskListSize = taskList.size
                taskList.forEach {
                    if(it.doneAt == null){
                        // 完了日時が入っていないタスクのみカウントする
                        activeTaskCount++
                    }
                }
            }else{
                taskListSize = 0
            }

            active_item_count_view.text = activeTaskCount.toString() + " items left"


            val completeTaskCount = taskListSize - activeTaskCount
            if(completeTaskCount > 0){
                clear_completed_button.visibility = View.VISIBLE
            }else{
                clear_completed_button.visibility = View.INVISIBLE
            }

            if(completeTaskCount == taskListSize && completeTaskCount > 0){
                // 完了が1つ以上あり、全て完了の場合、全完了ボタンの表示を濃くする
                all_complete_switch.alpha = 1f
            }else{
                all_complete_switch.alpha = 0.2f
            }

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
