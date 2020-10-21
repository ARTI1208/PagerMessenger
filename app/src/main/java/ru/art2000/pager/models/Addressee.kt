package ru.art2000.pager.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addressees")
data class Addressee(
    @NonNull
    @PrimaryKey
    val number: Int,
    val nickname: String? = null
) {
    fun toDisplayName(): String =
        if (nickname.isNullOrEmpty()) number.toString() else "$nickname ($number)"
}