package ru.art2000.pager.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.art2000.pager.models.Chat

@Dao
abstract class ChatsDao {

    @Query("SELECT * FROM chats")
    abstract fun all(): List<Chat>

    @Query("SELECT * FROM chats ORDER BY lastMessageId")
    abstract fun liveAll(): LiveData<List<Chat>>

    @Query("SELECT * FROM chats WHERE addresseeNumber = :number")
    abstract fun byAddresseeNumber(number: Int): Chat?

    @Insert
    abstract fun insertChat(chat: Chat): Long

    @Update
    abstract fun updateChat(chat: Chat): Int

    @Delete
    abstract fun deleteChat(chat: Chat): Int
}