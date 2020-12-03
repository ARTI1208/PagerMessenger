package ru.art2000.pager.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "addressees")
data class Addressee(
    @NonNull
    @PrimaryKey
    val number: Int,
    val nickname: String? = null
) : Parcelable {
    fun toDisplayName(): String =
        if (nickname.isNullOrEmpty()) number.toString() else "$nickname ($number)"
}