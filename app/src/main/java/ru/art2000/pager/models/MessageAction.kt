package ru.art2000.pager.models

interface MessageAction {

    val name: Int

    operator fun invoke(message: Message)

    fun displayOnSelect(message: Message): Boolean

    fun displayOnClick(message: Message): Boolean
}