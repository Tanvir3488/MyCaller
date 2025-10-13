package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    suspend fun getContacts(limit: Int, offset: Int): List<Contact>
    suspend fun getContacts(): Flow<List<Contact>>
    suspend fun searchContacts(query: String, limit: Int, offset: Int): List<Contact>
    suspend fun getContactByNumber(number: String): Contact?
    suspend fun syncContacts()
}
