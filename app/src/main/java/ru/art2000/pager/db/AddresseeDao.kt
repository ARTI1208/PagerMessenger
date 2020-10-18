package ru.art2000.pager.db

import androidx.room.Dao
import androidx.room.Query
import ru.art2000.pager.models.Addressee

@Dao
interface AddresseeDao {

    @Query("SELECT * FROM addressees")
    fun all(): List<Addressee>

    @Query("SELECT * FROM addressees WHERE number = :number")
    fun byNumber(number: Int): Addressee
}