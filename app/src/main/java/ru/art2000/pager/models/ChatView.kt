package ru.art2000.pager.models

import androidx.room.Embedded

data class ChatView(
    @Embedded val addressee: Addressee,
    @Embedded val draft: MessageDraft?,
    @Embedded val lastMessage: Message?
) {

    val chatPreview: MessageLike? get() = if (draft?.text.isNullOrEmpty()) lastMessage else draft
}