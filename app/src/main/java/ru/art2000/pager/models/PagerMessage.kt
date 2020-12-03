package ru.art2000.pager.models

data class PagerMessage(
    override val chatId: Int,
    override val text: String,
    override val settings: Int
) : MessageLike
