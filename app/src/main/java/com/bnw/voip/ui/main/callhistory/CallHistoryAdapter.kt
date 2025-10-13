package com.bnw.voip.ui.main.callhistory

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bnw.voip.R
import com.bnw.voip.databinding.ItemCallHistoryBinding
import com.bnw.voip.utils.AppConstants.CALL_TYPE_ANSWERED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_INCOMING
import com.bnw.voip.utils.AppConstants.CALL_TYPE_MISSED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_OUTGOING
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CallHistoryAdapter(
    private val onCallAgainClickListener: (String) -> Unit
) : ListAdapter<CallLogItem, CallHistoryAdapter.CallHistoryViewHolder>(CallLogsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CallHistoryViewHolder {
        return CallHistoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onCallAgainClickListener)
    }

    class CallHistoryViewHolder private constructor(private val binding: ItemCallHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CallLogItem, onCallAgainClickListener: (String) -> Unit) {
            binding.callerName.text = item.contactName ?: item.callLog.phoneNumber
            binding.phoneNumber.text = item.callLog.phoneNumber
            val durationInSeconds = item.callLog.callDuration // already in seconds
            val minutes = durationInSeconds / 60
            val seconds = durationInSeconds % 60
            val formattedDuration = String.format("%02d:%02d min", minutes, seconds)
            binding.callDuration.text = formattedDuration
            binding.callDateTime.text = formatDateTime(item.callLog.callStartTime)
            binding.callType.text = item.callLog.callType.uppercase(Locale.ROOT)

            when (item.callLog.callType) {
                CALL_TYPE_INCOMING -> {
                    binding.callTypeIcon.setColorFilter(Color.GREEN)
                    binding.callType.setTextColor(Color.GREEN)
                } // Replace with actual incoming icon
                CALL_TYPE_OUTGOING, CALL_TYPE_ANSWERED -> {
                    binding.callTypeIcon.setColorFilter(Color.BLUE)
                    binding.callType.setTextColor(Color.BLUE)
                } // Replace with actual outgoing icon
                CALL_TYPE_MISSED -> {
                    binding.callTypeIcon.setColorFilter(Color.RED)
                    binding.callType.setTextColor(Color.RED)
                } // Replace with actual missed icon
            }

            binding.callAgainButton.setOnClickListener {
                onCallAgainClickListener(item.callLog.phoneNumber)
            }
        }

        private fun formatDateTime(timestamp: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            return if (DateUtils.isToday(timestamp)) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
            } else {
                SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()).format(Date(timestamp))
            }
        }

        companion object {
            fun from(parent: ViewGroup): CallHistoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCallHistoryBinding.inflate(layoutInflater, parent, false)
                return CallHistoryViewHolder(binding)
            }
        }
    }
}

class CallLogsDiffCallback : DiffUtil.ItemCallback<CallLogItem>() {
    override fun areItemsTheSame(oldItem: CallLogItem, newItem: CallLogItem):
            Boolean {
        return oldItem.callLog.id == newItem.callLog.id
    }

    override fun areContentsTheSame(oldItem: CallLogItem, newItem: CallLogItem):
            Boolean {
        return oldItem == newItem
    }
}
