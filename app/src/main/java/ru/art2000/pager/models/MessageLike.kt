package ru.art2000.pager.models

interface MessageLike {

    val chatId: Int

    val text: String

    val settings: Int

    val time: Long

}