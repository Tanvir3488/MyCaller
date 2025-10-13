package com.bnw.voip.ui.main.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private var offset = 0
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    init {
        loadMoreContacts()

        _searchQuery
            .debounce(300)
            .onEach {
                resetAndLoadContacts()
            }
            .launchIn(viewModelScope)
    }

    fun loadMoreContacts() {
        if (isLoading || isLastPage) return

        viewModelScope.launch {
            isLoading = true
            val newContacts = getContactsUseCase(_searchQuery.value, pageSize, offset)
            if (newContacts.size < pageSize) {
                isLastPage = true
            }
            _contacts.value += newContacts
            offset += newContacts.size
            isLoading = false
        }
    }

    fun searchContacts(query: String) {
        _searchQuery.value = query
    }

    private fun resetAndLoadContacts() {
        offset = 0
        isLastPage = false
        _contacts.value = emptyList()
        loadMoreContacts()
    }
}
