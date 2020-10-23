package ru.art2000.pager.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.Message

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages WHERE chatId = :id")
    fun allByChatId(id: Int): List<Message>

    @Query("SELECT * FROM messages WHERE chatId = :id")
    fun liveAllByChatId(id: Int): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id")
    fun byMessageId(id: Int): Message?

    @Insert
    fun insertMessage(message: Message): Long

    @Delete
    fun deleteMessage(message: Message): Int

    @Update
    fun updateChat(chat: Chat): Int

    @Transaction
    fun safeInsertMessage(message: Message) {
        val id = insertMessage(message)
        updateChat(Chat(message.chatId, id.toInt()))
    }
}