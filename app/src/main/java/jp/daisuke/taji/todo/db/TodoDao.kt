package jp.daisuke.taji.todo.db

import androidx.room.*


@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(todo: Todo)

    @Query("SELECT * FROM todo")
    fun findAll(): List<Todo>

    @Update
    fun update(todo: Todo)

    @Delete
    fun delete(todo: Todo)
}