package com.bnw.voip.ui.main.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter { phoneNumber ->
            sipManager.call(phoneNumber)
        }
        
        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter
            
            // Add scroll listener for better performance
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Could add effects here like hiding search bar on scroll
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
                    contactAdapter.submitList(contacts)
                    updateUIState(contacts, viewModel.isLoading.value)
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    updateUIState(viewModel.contacts.value, isLoading)
                }
            }
        }
    }

    private fun updateUIState(contacts: List<Contact>, isLoading: Boolean) {
        when {
            isLoading -> {
                binding.loadingStateLayout.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
            }
            contacts.isEmpty() -> {
                binding.loadingStateLayout.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            }
            else -> {
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
