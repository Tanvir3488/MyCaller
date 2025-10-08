package com.bnw.voip.domain.usecase

import com.bnw.voip.data.repository.ContactRepository
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke() {
        contactRepository.syncContacts()
    }
}
