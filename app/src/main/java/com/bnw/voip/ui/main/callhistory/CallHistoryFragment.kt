package com.bnw.voip.ui.main.callhistory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bnw.voip.databinding.FragmentCallHistoryBinding
import com.bnw.voip.ui.incommingcall.CallingActivity
import com.bnw.voip.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class CallHistoryFragment : Fragment() {

    private var _binding: FragmentCallHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CallHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallHistoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = CallHistoryAdapter(){
            viewModel.callNumber(it)
        }
        binding.callHistoryRecyclerView.adapter = adapter

        viewModel.callHistory.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.navigationEvents.collect {
                val intent = Intent(requireContext(), CallingActivity::class.java).apply {
                    putExtra(AppConstants.PHONE_NUMBER, it)
                }
                startActivity(intent)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
