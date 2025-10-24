package com.bnw.voip.ui.main.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.databinding.FragmentContactsBinding
import com.bnw.voip.voip.CustomeSipManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private lateinit var contactAdapter: ContactAdapter

    @Inject
    lateinit var sipManager: CustomeSipManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeContacts()
        observeLoadingState()
        observeLoadingMoreState()
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter { phoneNumber ->
            sipManager.call(phoneNumber)
        }
        
        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter
            
            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    Log.d("ContactsFragment", "onScrolled called - dx: $dx, dy: $dy")
                    
                    if (dy > 0) { // Only trigger when scrolling down
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        val totalItemCount = layoutManager.itemCount
                        
                        Log.d("ContactsFragment", "Scrolled down - lastVisible: $lastVisibleItemPosition, totalItems: $totalItemCount, adapter size: ${contactAdapter.itemCount}")
                        
                        if (viewModel.shouldLoadMore(lastVisibleItemPosition)) {
                            Log.d("ContactsFragment", "Triggering loadMoreContacts")
                            viewModel.loadMoreContacts()
                        }
                    }
                }
            })
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchContacts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeContacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contacts.collect { contacts ->
                    Log.d("ContactsFragment", "Received ${contacts.size} contacts from ViewModel")
                    contactAdapter.submitList(contacts) {
                        Log.d("ContactsFragment", "Adapter updated - itemCount: ${contactAdapter.itemCount}")
                    }
                    updateUIState(contacts, viewModel.isLoading.value, viewModel.isLoadingMore.value)
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    updateUIState(viewModel.contacts.value, isLoading, viewModel.isLoadingMore.value)
                }
            }
        }
    }

    private fun observeLoadingMoreState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingMore.collect { isLoadingMore ->
                    updateUIState(viewModel.contacts.value, viewModel.isLoading.value, isLoadingMore)
                }
            }
        }
    }

    private fun updateUIState(contacts: List<Contact>, isLoading: Boolean, isLoadingMore: Boolean) {
        Log.d("ContactsFragment", "updateUIState - contacts: ${contacts.size}, isLoading: $isLoading, isLoadingMore: $isLoadingMore")
        
        when {
            isLoading && contacts.isEmpty() -> {
                Log.d("ContactsFragment", "Showing loading state")
                binding.loadingStateLayout.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
            }
            contacts.isEmpty() && !isLoading -> {
                Log.d("ContactsFragment", "Showing empty state")
                binding.loadingStateLayout.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            }
            else -> {
                Log.d("ContactsFragment", "Showing contacts list")
                binding.loadingStateLayout.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
