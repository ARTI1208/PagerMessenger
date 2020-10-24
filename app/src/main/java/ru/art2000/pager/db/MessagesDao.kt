package ru.art2000.pager.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import ru.art2000.pager.models.Chat
import ru.art2000.pager.models.Message

@Dao
abstract class MessagesDao(private val messagesDatabase: MessagesDatabase) {

    @Query("SELECT * FROM messages WHERE chatId = :id AND status != ${Message.STATUS_DRAFT}")
    public abstract fun allByChatId(id: Int): List<Message>

    @Query("SELECT * FROM messages WHERE chatId = :id AND status != ${Message.STATUS_DRAFT}")
    public abstract fun liveAllByChatId(id: Int): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id")
    public abstract fun byMessageId(id: Int): Message?

    @Query("SELECT * FROM messages WHERE chatId = :id AND status != ${Message.STATUS_DRAFT} ORDER BY id DESC LIMIT 1")
    public abstract fun lastMessageForChat(id: Int): Message?

    @Query("SELECT * FROM messages WHERE chatId = :id ORDER BY id DESC LIMIT 1")
    public abstract fun lastMessageForChatWithDraft(id: Int): Message?

    @Insert
    protected abstract fun insertMessage(message: Message): Long

    @Delete
    public abstract fun deleteMessage(message: Message): Int

    @Query("UPDATE messages SET text = :newText, settings = :settings, time = :time WHERE chatId = :chatId AND status = ${Message.STATUS_DRAFT}")
    protected abstract fun updateDraft(chatId: Int, newText: String, settings: Int, time: Long = System.currentTimeMillis())

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    protected abstract fun updateDraft2(chatId: Int, newText: String, settings: Int, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND status = ${Message.STATUS_DRAFT}")
    public abstract fun getDraftForChat(chatId: Int): Message?

    @Query("DELETE FROM messages WHERE chatId = :chatId AND status = ${Message.STATUS_DRAFT}")
    public abstract fun deleteDrafts(chatId: Int): Int

    @Transaction
    public open fun insertOrUpdateDraft(message: Message) {
        if (!message.isDraft) return
        val messageId = if (getDraftForChat(message.chatId) == null) {
            Log.e("draftUpdate", "no prev draft for chat ${message.chatId}")
            insertMessage(message)
        } else {
            Log.e("draftUpdate", "found prev version ${message.chatId}")
            updateDraft(message.chatId, message.text, message.settings, message.time)
            getDraftForChat(message.chatId)?.id!!
        }
        messagesDatabase.chatsDao().updateChat(Chat(message.chatId, messageId.toInt()))
    }

    @Transaction
    public open fun safeInsertMessage(message: Message) {
        if (message.isDraft) {
            insertOrUpdateDraft(message)
            return
        }

        val id = insertMessage(message)
        messagesDatabase.chatsDao().updateChat(Chat(message.chatId, id.toInt()))
    }
}