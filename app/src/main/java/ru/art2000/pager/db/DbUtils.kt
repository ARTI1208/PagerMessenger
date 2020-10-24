package ru.art2000.pager.db

import android.content.Context

fun <T> addresseeTable(context: Context, actions: AddresseeDao.() -> T): T
        = MessagesDatabase.getInstance(context).addresseeTable(actions)

fun <T> chatsTable(context: Context, actions: ChatsDao.() -> T): T
        = MessagesDatabase.getInstance(context).chatsTable(actions)

fun <T> messagesTable(context: Context, actions: MessagesDao.() -> T): T
        = MessagesDatabase.getInstance(context).messagesTable(actions)