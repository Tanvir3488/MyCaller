package com.bnw.voip.ui.main.callhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.repository.ContactRepository
import com.bnw.voip.domain.usecase.GetCallLogsUseCase
import com.bnw.voip.domain.usecase.call.MakeCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallLogItem(
    val callLog: CallLogs,
    val contactName: String?
)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    getCallLogsUseCase: GetCallLogsUseCase,
    contactRepository: ContactRepository,
    private val makeCallUseCase: MakeCallUseCase
) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents = _navigationEvents.asSharedFlow()

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

    fun callNumber(number: String) {
        viewModelScope.launch {
            makeCallUseCase(number)
            _navigationEvents.emit(number)
        }
    }
}
