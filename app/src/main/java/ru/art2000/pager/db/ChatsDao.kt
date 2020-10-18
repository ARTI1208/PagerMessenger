package ru.art2000.pager.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.art2000.pager.models.Chat

@Dao
interface ChatsDao {

    @Query("SELECT * FROM chats")
    fun all(): List<Chat>

    @Query("SELECT * FROM chats")
    fun liveAll(): LiveData<List<Chat>>

    @Query("SELECT * FROM chats WHERE addresseeNumber = :number")
    fun byAddresseeNumber(number: Int): Chat

    @Insert
    fun insertChat(chat: Chat): Long

    @Update
    fun updateChat(chat: Chat): Int

    @Delete
    fun deleteChat(chat: Chat): Int
}