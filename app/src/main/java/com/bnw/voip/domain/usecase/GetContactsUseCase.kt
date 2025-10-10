package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.Contact
import com.bnw.voip.data.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(query: String): Flow<List<Contact>> {
        return if (query.isBlank()) {
            contactRepository.getContacts()
        } else {
            contactRepository.searchContacts(query)
        }
    }
}
