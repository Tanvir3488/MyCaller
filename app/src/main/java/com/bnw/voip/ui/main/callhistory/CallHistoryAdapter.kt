package com.bnw.voip.ui.main.callhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.databinding.ItemCallHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class CallHistoryAdapter : ListAdapter<CallLogs, CallHistoryAdapter.CallHistoryViewHolder>(CallLogsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CallHistoryViewHolder {
        return CallHistoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class CallHistoryViewHolder private constructor(private val binding: ItemCallHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CallLogs) {
            binding.callerName.text = item.callerName
            binding.phoneNumber.text = item.phoneNumber
            binding.callTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.callStartTime))
            // Here you can set the call type icon based on item.callStatus
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

class CallLogsDiffCallback : DiffUtil.ItemCallback<CallLogs>() {
    override fun areItemsTheSame(oldItem: CallLogs, newItem: CallLogs):
            Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CallLogs, newItem: CallLogs):
            Boolean {
        return oldItem == newItem
    }
}