package com.bnw.voip.ui.main.callhistory

import android.content.Intent
import android.os.Bundle
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
import com.bnw.voip.R
import com.bnw.voip.databinding.FragmentCallHistoryBinding
import com.bnw.voip.ui.incommingcall.CallingActivity
import com.bnw.voip.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallHistoryFragment : Fragment() {

    private var _binding: FragmentCallHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CallHistoryViewModel by viewModels()
    private lateinit var adapter: CallHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallHistoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        
        setupRecyclerView()
        observeCallHistory()
        observeNavigationEvents()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CallHistoryAdapter { phoneNumber ->
            viewModel.callNumber(phoneNumber)
        }
        
        binding.callHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CallHistoryFragment.adapter
            
            // Add scroll listener for better UX
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Could add fade effect for toolbar here
                }
            })
        }

        // Auto-scroll to top when new items are added
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.callHistoryRecyclerView.smoothScrollToPosition(0)
                }
            }
        })
    }

    private fun observeCallHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callHistory.collect { callHistory ->
                    adapter.submitList(callHistory)
                    
                    // Show/hide empty state
                    if (callHistory.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.callHistoryRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.callHistoryRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvents.collect { phoneNumber ->
                    val intent = Intent(requireContext(), CallingActivity::class.java).apply {
                        putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
