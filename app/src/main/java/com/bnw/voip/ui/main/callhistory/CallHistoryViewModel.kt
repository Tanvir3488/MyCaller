package com.bnw.voip.ui.main.callhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.repository.ContactRepository
import com.bnw.voip.domain.usecase.GetCallLogsUseCase
import com.bnw.voip.domain.usecase.call.MakeCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallLogItem(
    val callLog: CallLogs,
    val contactName: String?
)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val getCallLogsUseCase: GetCallLogsUseCase,
    private val contactRepository: ContactRepository,
    private val makeCallUseCase: MakeCallUseCase
) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    private val _callHistory = MutableStateFlow<List<CallLogItem>>(emptyList())
    val callHistory = _callHistory.asStateFlow()

    private var offset = 0
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    init {
        loadMoreCallLogs()
    }

    fun loadMoreCallLogs() {
        if (isLoading || isLastPage) return

        viewModelScope.launch {
            isLoading = true
            val newLogs = getCallLogsUseCase(pageSize, offset)
            if (newLogs.size < pageSize) {
                isLastPage = true
            }
            val contacts = contactRepository.getContacts().first()
            val newCallLogItems = newLogs.map { callLog ->
                val contact = contacts.find { contact ->
                    contact.phoneNumbers.any { phoneNumber ->
                        phoneNumber == callLog.phoneNumber
                    }
                }
                CallLogItem(callLog, contact?.name)
            }
            _callHistory.value += newCallLogItems
            offset += newLogs.size
            isLoading = false
        }
    }

    fun callNumber(number: String) {
        viewModelScope.launch {
            makeCallUseCase(number)
            _navigationEvents.emit(number)
        }
    }
}
