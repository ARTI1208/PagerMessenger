package ru.art2000.pager.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.art2000.pager.models.MessageDraft

@Dao
interface DraftsDao {

    @Query("SELECT * FROM drafts WHERE draft_chatId = :id AND draft_text IS NOT NULL AND draft_text != ''")
    public fun draftNonEmptyTextForChat(id: Int): MessageDraft?

    @Query("SELECT * FROM drafts WHERE draft_chatId = :id")
    public fun draftForChat(id: Int): MessageDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public fun updateDraft(draft: MessageDraft)

    @Query("UPDATE drafts SET draft_text = '' WHERE draft_chatId = :id")
    public fun deleteDraftForChat(id: Int)
}