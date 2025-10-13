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

    override suspend fun getContacts(limit: Int, offset: Int): List<Contact> {
        return contactDao.getContacts(limit, offset)
    }

    override suspend fun getContacts(): Flow<List<Contact>> {
        return contactDao.getContacts()
    }

    override suspend fun searchContacts(query: String, limit: Int, offset: Int): List<Contact> {
        return contactDao.searchContacts(query, limit, offset)
    }

    override suspend fun getContactByNumber(number: String): Contact? {
        return contactDao.getContactByNumber(number)
    }

    @SuppressLint("Range")
    override suspend fun syncContacts() {
        val sharedPreferences = context.getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
        val lastSyncTimestamp = sharedPreferences.getLong("last_sync_timestamp", 0)

        val deviceContacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        val selection = "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
        val selectionArgs = arrayOf(lastSyncTimestamp.toString())

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val photoUri = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val lastUpdated = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP))
                val normalizedPhoneNumber = phoneNumber
                    .replace(Regex("[^0-9+]"), "") // remove everything except digits and +
                    .replace(" ", "")              // remove spaces
                    .trim()

                val existingContact = deviceContacts.find { c -> c.deviceContactId == id }
                if (existingContact != null) {
                    val updatedPhoneNumbers = existingContact.phoneNumbers.toMutableList()
                    if (!updatedPhoneNumbers.contains(normalizedPhoneNumber)) {
                        updatedPhoneNumbers.add(normalizedPhoneNumber)
                    }
                    val index = deviceContacts.indexOf(existingContact)
                    deviceContacts[index] = existingContact.copy(phoneNumbers = updatedPhoneNumbers)
                } else {
                    deviceContacts.add(
                        Contact(
                            deviceContactId = id,
                            name = name,
                            phoneNumbers = listOf(normalizedPhoneNumber),
                            photoUri = photoUri,
                            lastUpdatedTimestamp = lastUpdated
                        )
                    )
                }
            }
        }
        cursor?.close()

        for (contact in deviceContacts) {
            val existingContact = contactDao.getContactByDeviceContactId(contact.deviceContactId)
            if (existingContact == null) {
                contactDao.insert(contact)
            } else {
                contactDao.update(contact.copy(id = existingContact.id))
            }
        }

        with(sharedPreferences.edit()) {
            putLong("last_sync_timestamp", System.currentTimeMillis())
            apply()
        }
    }
}
