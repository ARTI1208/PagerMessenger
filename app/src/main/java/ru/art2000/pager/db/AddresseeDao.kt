package ru.art2000.pager.db

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import ru.art2000.pager.models.Addressee
import ru.art2000.pager.models.ChatView

@Dao
abstract class AddresseeDao(private val messagesDatabase: MessagesDatabase) {

    @Query("SELECT * FROM addressees")
    abstract fun all(): List<Addressee>

    @Query("SELECT * FROM addressees")
    abstract fun liveAll(): LiveData<List<Addressee>>

    @Query("SELECT * FROM addressees WHERE number = :number")
    abstract fun byNumber(number: Int): Addressee?


    @Transaction
    @Query("SELECT * FROM addressees")
    abstract fun liveAllJoined(): LiveData<List<ChatView>>

    @Transaction
    @Query("SELECT * FROM addressees")
    abstract fun sourceAllJoined(): PagingSource<Int, ChatView>

    @Transaction
    @Query("with addresseeWithDraft as (select * from addressees Left JOIN drafts on addressees.number = drafts.draft_chatId AND draft_text IS NOT NULL AND draft_text != ''), msg as (select m.* from messages m where (select max(messages.time) from messages where messages.chatId = m.chatId) = m.time) SELECT * from addresseeWithDraft left join msg on addresseeWithDraft.number = msg.chatId ORDER BY max(coalesce(addresseeWithDraft.draft_time, -1), msg.time) DESC")
    abstract fun sourceAllJoinedOrdered(): PagingSource<Int, ChatView>

    @Insert
    abstract fun insertAddressee(addressee: Addressee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateAddressee(addressee: Addressee): Long

    @Delete
    abstract fun deleteAddressees(addressees: Collection<Addressee>): Int

    @Transaction
    open fun wipeAddresseeChats(chats: Collection<Addressee>) {
        deleteAddressees(chats)
        messagesDatabase.messagesTable {
            chats.forEach {
                deleteAllMessages(it.number)
            }
        }
    }
}