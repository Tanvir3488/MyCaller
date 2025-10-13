package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.Contact
import com.bnw.voip.data.repository.ContactRepository
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(query: String, limit: Int, offset: Int): List<Contact> {
        return if (query.isBlank()) {
            contactRepository.getContacts(limit, offset)
        } else {
            contactRepository.searchContacts(query, limit, offset)
        }
    }
}
