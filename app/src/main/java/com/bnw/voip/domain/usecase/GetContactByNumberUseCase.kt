package com.bnw.voip.domain.usecase

import android.util.Log
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.data.repository.ContactRepository
import javax.inject.Inject

class GetContactByNumberUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(number: String): Contact? {
        val contact = contactRepository.getContactByNumber(number)
        Log.e("GetContactByNumber", "Fetching contact for number: $number contuct ${contact}")
        return contact
    }
}
