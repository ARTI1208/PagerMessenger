package ru.art2000.pager.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatView(val addressee: Addressee, val lastMessage: Message?) : Parcelable {

    public val chat: Chat get() = Chat(addressee.number, lastMessage?.id ?: -1)
}