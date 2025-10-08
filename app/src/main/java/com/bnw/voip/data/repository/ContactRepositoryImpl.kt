package com.bnw.voip.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import com.bnw.voip.data.db.ContactDao
import com.bnw.voip.data.entity.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    @ApplicationContext private val context: Context
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> {
        return contactDao.getAll()
    }

    @SuppressLint("Range")
    override suspend fun syncContacts() {
        val deviceContacts = mutableMapOf<String, MutableList<String>>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val normalizedPhoneNumber = phoneNumber.replace("[^0-9+]", "")
                deviceContacts.getOrPut(name) { mutableListOf() }.add(normalizedPhoneNumber)
            }
        }
        cursor?.close()

        for ((name, phoneNumbers) in deviceContacts) {
            val existingContact = contactDao.getContactByName(name)
            if (existingContact == null) {
                contactDao.insert(Contact(name = name, phoneNumbers = phoneNumbers))
            } else {
                val updatedPhoneNumbers = existingContact.phoneNumbers.toMutableList()
                var hasChanges = false
                for (phoneNumber in phoneNumbers) {
                    if (!updatedPhoneNumbers.contains(phoneNumber)) {
                        updatedPhoneNumbers.add(phoneNumber)
                        hasChanges = true
                    }
                }
                if (hasChanges) {
                    contactDao.insert(existingContact.copy(phoneNumbers = updatedPhoneNumbers))
                }
            }
        }
    }
}
