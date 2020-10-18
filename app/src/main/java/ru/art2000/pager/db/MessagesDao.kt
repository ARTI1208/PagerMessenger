package ru.art2000.pager.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.art2000.pager.models.Message

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages WHERE chatId = :id")
    fun allByChatId(id: Int): List<Message>

    @Query("SELECT * FROM messages WHERE chatId = :id")
    fun liveAllByChatId(id: Int): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id")
    fun byMessageId(id: Int): Message

    @Insert
    fun insertMessage(message: Message): Long

    @Delete
    fun deleteMessage(message: Message): Int
}