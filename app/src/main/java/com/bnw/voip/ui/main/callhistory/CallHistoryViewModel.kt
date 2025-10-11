package com.bnw.voip.ui.main.callhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.repository.ContactRepository
import com.bnw.voip.domain.usecase.GetCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CallLogItem(
    val callLog: CallLogs,
    val contactName: String?
)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    getCallLogsUseCase: GetCallLogsUseCase,
    contactRepository: ContactRepository
) : ViewModel() {

    val callHistory = combine(
        getCallLogsUseCase(),
        contactRepository.getContacts()
    ) { callLogs, contacts ->
        callLogs.map { callLog ->
            val contact = contacts.find { contact ->
                contact.phoneNumbers.any { phoneNumber ->
                    phoneNumber == callLog.phoneNumber
                }
            }
            CallLogItem(callLog, contact?.name)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()
}