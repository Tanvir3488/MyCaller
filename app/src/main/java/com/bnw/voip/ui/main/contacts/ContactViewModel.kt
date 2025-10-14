package com.bnw.voip.ui.main.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                getContactsUseCase(query)
            }
            .onEach { newContacts ->
                _contacts.value = newContacts
            }
            .launchIn(viewModelScope)
    }

    fun searchContacts(query: String) {
        _searchQuery.value = query
    }
}
