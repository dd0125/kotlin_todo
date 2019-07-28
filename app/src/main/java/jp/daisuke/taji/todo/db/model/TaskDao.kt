package jp.daisuke.taji.todo.db.model

import androidx.room.*


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(task: Task)

    @Query("SELECT * FROM task")
    fun findAll(): List<Task>

    @Query("SELECT * FROM task where doneAt is null")
    fun findActive(): List<Task>

    @Query("SELECT * FROM task where doneAt is not null")
    fun findComplete(): List<Task>

    @Update
    fun update(task: Task)

    @Delete
    fun delete(task: Task)
}