package jp.daisuke.taji.todo.screen.tasklist

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import jp.daisuke.taji.todo.R
import jp.daisuke.taji.todo.db.model.Task
import java.util.*

class TaskListAdapter(context: Context, taskList: List<Task>) : ArrayAdapter<Task>(context, 0, taskList) {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var updateTaskFunction:((Task)->Unit)? = null
    fun setUpdateTaskFunction(function:(Task)->Unit) {
        updateTaskFunction = function
    }
    private var deleteTaskFunction:((Task)->Unit)? = null
    fun setDeleteTaskFunction(function:(Task)->Unit) {
        deleteTaskFunction = function
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        val holder: ViewHolder
        val task = getItem(position) as Task

        val nameTextView: TextView
        val doneCheckBox: CheckBox
        if (view == null) {
            view = layoutInflater.inflate(R.layout.task_list_item, parent, false) as View

            nameTextView = view.findViewById(R.id.name_text_view)

            doneCheckBox = view.findViewById(R.id.done_checkbox)

            val deleteButton: ImageView = view.findViewById(R.id.clear_button)
            deleteButton.setOnClickListener {
                deleteTaskFunction?.invoke(task)
            }

            holder = ViewHolder(
                nameTextView = nameTextView,
                doneCheckBox = doneCheckBox,
                deleteButton = deleteButton
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
            nameTextView = holder.nameTextView
            doneCheckBox = holder.doneCheckBox

        }

        val paint = nameTextView.paint
        paint.isAntiAlias = true
        if(task.doneAt != null){
            // 完了の場合
            nameTextView.setTextColor(Color.parseColor("#aaaaaa"))

            // 取り消し線をつける
            paint.flags = nameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }else{
            nameTextView.setTextColor(Color.parseColor("#222222"))

            // 取り消し線がついていたら外す
            paint.flags = nameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        nameTextView.text = task.name

        // 完了済のタスクの場合、Check を入れる
        doneCheckBox.setOnCheckedChangeListener(null)
        doneCheckBox.isChecked = (task.doneAt != null)
        doneCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                // 完了にする場合、完了日時を入れる
                task.doneAt = Date()
            }else{
                // 未完了にする場合、完了日時を削除
                task.doneAt = null
            }
            // Listenerを実行
            updateTaskFunction?.invoke(task)
        }


        view.setOnFocusChangeListener { v, hasFocus ->
            val clearButton = v.findViewById<View>(R.id.clear_button)
            if(hasFocus){
                clearButton.visibility = View.VISIBLE
            }else{
                clearButton.visibility = View.INVISIBLE

            }
        }

        return view
    }

    private data class ViewHolder(
        val nameTextView: TextView,
        val doneCheckBox: CheckBox,
        val deleteButton: ImageView
    )
}
