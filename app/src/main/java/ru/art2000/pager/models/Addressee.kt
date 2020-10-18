package ru.art2000.pager.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addressees")
data class Addressee(
    @NonNull
    @PrimaryKey
    val number: Int,
    var nickname: String? = null
)