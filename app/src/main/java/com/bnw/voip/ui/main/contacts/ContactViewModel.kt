package com.bnw.voip.ui.main.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    companion object {
        const val PAGE_SIZE = 30
        const val PREFETCH_THRESHOLD = 10
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 0
    private var hasMoreItems = true
    private var isSearchMode = false
    private var lastSearchQuery = ""
    private var searchJob: Job? = null

    init {
        loadInitialContacts()
    }

    private fun loadInitialContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                currentPage = 0
                hasMoreItems = true
                isSearchMode = false
                
                Log.d("ContactViewModel", "Loading initial contacts, page: 0, pageSize: $PAGE_SIZE")
                val result = getContactsUseCase.getContactsPaginated(page = 0, pageSize = PAGE_SIZE)
                Log.d("ContactViewModel", "Initial load result: ${result.contacts.size} contacts, hasMore: ${result.hasMoreItems}, total: ${result.totalCount}")
                
                _contacts.value = result.contacts
                hasMoreItems = result.hasMoreItems
                currentPage = 1
            } catch (e: Exception) {
                Log.e("ContactViewModel", "Error loading initial contacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchContacts(query: String) {
        _searchQuery.value = query
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isBlank()) {
            if (isSearchMode) {
                Log.d("ContactViewModel", "Search cleared, loading initial contacts")
                loadInitialContacts()
            }
            return
        }

        if (query == lastSearchQuery) return
        
        // Debounce search
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            
            lastSearchQuery = query
            isSearchMode = true
            
            _isLoading.value = true
            try {
                currentPage = 0
                hasMoreItems = true
                
                Log.d("ContactViewModel", "Searching for: '$query', page: 0")
                val result = getContactsUseCase.searchContactsPaginated(query, page = 0, pageSize = PAGE_SIZE)
                Log.d("ContactViewModel", "Search result: ${result.contacts.size} contacts, hasMore: ${result.hasMoreItems}, total: ${result.totalCount}")
                
                _contacts.value = result.contacts
                hasMoreItems = result.hasMoreItems
                currentPage = 1
            } catch (e: Exception) {
                Log.e("ContactViewModel", "Error searching contacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreContacts() {
        Log.d("ContactViewModel", "loadMoreContacts called - hasMoreItems: $hasMoreItems, isLoading: ${_isLoading.value}, isLoadingMore: ${_isLoadingMore.value}")
        
        if (!hasMoreItems || _isLoading.value || _isLoadingMore.value) {
            Log.d("ContactViewModel", "loadMoreContacts blocked - hasMoreItems: $hasMoreItems, isLoading: ${_isLoading.value}, isLoadingMore: ${_isLoadingMore.value}")
            return
        }
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                Log.d("ContactViewModel", "Loading more contacts, page: $currentPage, isSearchMode: $isSearchMode")
                
                val result = if (isSearchMode) {
                    getContactsUseCase.searchContactsPaginated(lastSearchQuery, page = currentPage, pageSize = PAGE_SIZE)
                } else {
                    getContactsUseCase.getContactsPaginated(page = currentPage, pageSize = PAGE_SIZE)
                }
                
                Log.d("ContactViewModel", "Load more result: ${result.contacts.size} new contacts, hasMore: ${result.hasMoreItems}")
                
                val currentContacts = _contacts.value.toMutableList()
                currentContacts.addAll(result.contacts)
                _contacts.value = currentContacts
                
                Log.d("ContactViewModel", "Total contacts after load more: ${currentContacts.size}")
                
                hasMoreItems = result.hasMoreItems
                currentPage++
            } catch (e: Exception) {
                Log.e("ContactViewModel", "Error loading more contacts", e)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun shouldLoadMore(lastVisibleItemPosition: Int): Boolean {
        val totalItems = _contacts.value.size
        val remainingItems = totalItems - lastVisibleItemPosition - 1 // -1 because position is 0-based
        val shouldLoad = hasMoreItems && remainingItems <= PREFETCH_THRESHOLD
        Log.d("ContactViewModel", "shouldLoadMore: lastVisible=$lastVisibleItemPosition, totalItems=$totalItems, remaining=$remainingItems, threshold=$PREFETCH_THRESHOLD, hasMore=$hasMoreItems, shouldLoad=$shouldLoad")
        return shouldLoad
    }
}
