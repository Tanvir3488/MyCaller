package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun syncContacts()
}
