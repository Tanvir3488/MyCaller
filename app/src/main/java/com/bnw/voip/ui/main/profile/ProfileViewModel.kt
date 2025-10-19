package com.bnw.voip.ui.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.domain.usecase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.bnw.voip.voip.CustomeSipManager

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val sipManager: CustomeSipManager,
    getUserUseCase: GetUserUseCase
) : ViewModel() {

    val user: StateFlow<com.bnw.voip.data.model.User?> = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun logout() {
        viewModelScope.launch {
            sipManager.logout()
            userManager.clearUser()
        }
    }
}