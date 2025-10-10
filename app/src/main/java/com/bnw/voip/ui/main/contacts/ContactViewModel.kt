package com.bnw.voip.ui.main.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val contacts: StateFlow<List<Contact>> = _searchQuery
        .flatMapLatest { query ->
            getContactsUseCase(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchContacts(query: String) {
        _searchQuery.value = query
    }
}
