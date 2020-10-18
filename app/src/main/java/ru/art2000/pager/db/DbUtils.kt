package ru.art2000.pager.db

import android.content.Context

fun <T> addresseeTable(context: Context, actions: AddresseeDao.() -> T): T {
    return actions(MessagesDatabase.getInstance(context).addresseeDao())
}

fun <T> chatsTable(context: Context, actions: ChatsDao.() -> T): T {
    return actions(MessagesDatabase.getInstance(context).chatsDao())
}

fun <T> messagesTable(context: Context, actions: MessagesDao.() -> T): T {
    return actions(MessagesDatabase.getInstance(context).messagesDao())
}