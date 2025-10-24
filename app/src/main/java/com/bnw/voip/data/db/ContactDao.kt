package com.bnw.voip.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bnw.voip.data.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Query("SELECT * FROM contacts WHERE REPLACE(name, ' ', '') LIKE '%' || REPLACE(:query, ' ', '') || '%' OR phoneNumbers LIKE '%' || :query || '%'")
    suspend fun searchContacts(query: String): List<Contact>

    @Query("SELECT * FROM contacts")
     fun getContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getContactsPaginated(limit: Int, offset: Int): List<Contact>

    @Query("SELECT * FROM contacts WHERE REPLACE(name, ' ', '') LIKE '%' || REPLACE(:query, ' ', '') || '%' OR phoneNumbers LIKE '%' || :query || '%' ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun searchContactsPaginated(query: String, limit: Int, offset: Int): List<Contact>

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactsCount(): Int

    @Query("SELECT COUNT(*) FROM contacts WHERE REPLACE(name, ' ', '') LIKE '%' || REPLACE(:query, ' ', '') || '%' OR phoneNumbers LIKE '%' || :query || '%'")
    suspend fun getSearchContactsCount(query: String): Int

    @Query("SELECT * FROM contacts WHERE phoneNumbers LIKE '%' || :number || '%'")
    suspend fun getContactByNumber(number: String): Contact?

    @Query("SELECT * FROM contacts WHERE name = :name")
    suspend fun getContactByName(name: String): Contact?

    @Query("SELECT * FROM contacts WHERE deviceContactId = :deviceContactId")
    suspend fun getContactByDeviceContactId(deviceContactId: Long): Contact?
}
