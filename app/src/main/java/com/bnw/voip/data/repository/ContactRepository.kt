package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.Contact
import kotlinx.coroutines.flow.Flow

data class PaginationResult(
    val contacts: List<Contact>,
    val hasMoreItems: Boolean,
    val totalCount: Int
)

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun searchContacts(query: String): List<Contact>
    suspend fun getContactByNumber(number: String): Contact?
    suspend fun syncContacts()
    suspend fun getContactsPaginated(page: Int, pageSize: Int): PaginationResult
    suspend fun searchContactsPaginated(query: String, page: Int, pageSize: Int): PaginationResult
}
