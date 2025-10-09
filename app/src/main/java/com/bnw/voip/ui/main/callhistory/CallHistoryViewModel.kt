package com.bnw.voip.ui.main.callhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.domain.usecase.GetCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val getCallLogsUseCase: GetCallLogsUseCase
) : ViewModel() {

    val callHistory: LiveData<List<CallLogs>> = getCallLogsUseCase().asLiveData()
}