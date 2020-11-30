package ru.art2000.pager.db

import androidx.paging.PagingSource
import androidx.room.*
import ru.art2000.pager.models.Message

@Suppress("DEPRECATION")
@Dao
abstract class MessagesDao(private val messagesDatabase: MessagesDatabase) {

    @Query("SELECT * FROM messages WHERE chatId = :id AND status >= ${Message.FIRST_VISIBLE_CODE} ORDER BY id DESC")
    public abstract fun pagedReversedVisibleMessages(id: Int): PagingSource<Int, Message>

    @Query("SELECT * FROM messages WHERE id = :id")
    public abstract fun byMessageId(id: Int): Message?

    @Query("SELECT * FROM messages WHERE chatId = :id ORDER BY id DESC LIMIT 1")
    public abstract fun lastMessageForChat(id: Int): Message?

    @Query("SELECT * FROM messages WHERE chatId = :id AND (text != '' OR status = ${Message.STATUS_CHAT_CREATED}) ORDER BY id DESC LIMIT 1")
    public abstract fun lastMessageForChatWithDraft(id: Int): Message?

    @Insert
    protected abstract fun insertMessage(message: Message): Long

    @Delete
    public abstract fun deleteMessage(message: Message): Int

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND status = ${Message.STATUS_CHAT_CREATED}")
    protected abstract fun getChatCreatedMessage(chatId: Int): Message?

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    public abstract fun deleteAllMessages(chatId: Int): Int

    public open fun insertChatCreatedMessage(message: Message): Boolean {
        if (message.status != Message.STATUS_CHAT_CREATED) return false

        return if (getChatCreatedMessage(message.chatId) == null) {
            insertMessage(message)
            true
        } else false
    }

    @Transaction
    public open fun safeInsertMessage(message: Message) {
        if (message.status == Message.STATUS_CHAT_CREATED) {
            insertChatCreatedMessage(message)
            return
        }

        messagesDatabase.draftsDao().deleteDraftForChat(message.chatId)
        insertMessage(message)
    }
}