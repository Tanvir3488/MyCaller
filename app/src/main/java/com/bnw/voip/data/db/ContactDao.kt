package com.bnw.voip.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bnw.voip.data.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phoneNumbers LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<Contact>>

    @Query("SELECT * FROM contacts")
    fun getAll(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE phoneNumbers LIKE '%' || :number || '%'")
    suspend fun getContactByNumber(number: String): Contact?

    @Query("SELECT * FROM contacts WHERE name = :name")
    suspend fun getContactByName(name: String): Contact?
}
