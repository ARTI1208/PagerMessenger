package ru.art2000.pager.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "chats")
data class Chat(
    @NonNull
    @PrimaryKey
    val addresseeNumber: Int,
    var lastMessageId: Int = -1
) : Parcelable