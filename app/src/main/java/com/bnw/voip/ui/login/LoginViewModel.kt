package com.bnw.voip.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.domain.usecase.call.LoginUseCase
import com.bnw.voip.domain.usecase.call.StartSipUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.voip.CallState
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userManager: UserManager,
    private val loginUseCase: LoginUseCase,
    private val sipUseCase: StartSipUseCase,
    getCallStateUseCase: GetCallStateUseCase
) : ViewModel() {

    val callState: Flow<CallState.State> = getCallStateUseCase()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            userManager.saveUserCredentials(username, password)
            loginUseCase(username, password)
            sipUseCase()
        }
    }
}
