package jp.daisuke.taji.todo

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import jp.daisuke.taji.todo.db.model.Task
import java.util.*

class TaskListAdapter(context: Context, taskList: List<Task>) : ArrayAdapter<Task>(context, 0, taskList) {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var updateTaskFunction:((Task)->Unit)? = null

    fun setUpdateTaskFunction(function:(Task)->Unit) {
        updateTaskFunction = function
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        val holder: ViewHolder
        val task = getItem(position) as Task

        val nameTextView: TextView
        if (view == null) {
            view = layoutInflater.inflate(R.layout.task_list_item, parent, false)
            nameTextView = view.findViewById(R.id.name_text_view)

            val doneButton: Button = view.findViewById(R.id.done_button)
            doneButton.setOnClickListener {
                if(task.doneAt != null){
                    // 完了の場合、キャンセルする
                    task.doneAt = null
                }else{
                    // 未完了の場合、完了させる
                    task.doneAt = Date()
                }
                // Listenerを実行
                updateTaskFunction?.invoke(task)
            }

            holder = ViewHolder(
                nameTextView = nameTextView,
                doneButton = doneButton
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
            nameTextView = holder.nameTextView

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
        holder.nameTextView.text = task.name

        return view
    }

    private data class ViewHolder(
        val nameTextView: TextView,
        val doneButton:Button
    )
}
