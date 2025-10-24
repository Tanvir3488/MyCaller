package com.bnw.voip.ui.main.callhistory

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            setupCallerInfo(item)
            setupCallDetails(item)
            setupCallTypeIndicator(item)
            setupActionButton(item, onCallAgainClickListener)
        }

        private fun setupCallerInfo(item: CallLogItem) {
            // Set caller name or phone number if no contact name
            binding.callerName.text = item.contactName ?: formatPhoneNumber(item.callLog.phoneNumber)

            // Always show the raw phone number in smaller text
            binding.phoneNumber.text = item.callLog.phoneNumber

            // Hide phone number row if it's the same as caller name (when no contact name)
            if (item.contactName == null) {
                binding.phoneNumber.visibility = View.GONE
            } else {
                binding.phoneNumber.visibility = View.VISIBLE
            }
        }

        private fun setupCallDetails(item: CallLogItem) {
            // Format and set call duration
            val durationInSeconds = item.callLog.callDuration
            if (durationInSeconds > 0L) {
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60
                binding.callDuration.text = String.format("%d:%02d min", minutes, seconds)
                binding.callDuration.visibility = View.VISIBLE
            } else {
                binding.callDuration.visibility = View.GONE
            }

            // Format and set call date/time
            binding.callDateTime.text = formatDateTime(item.callLog.callStartTime)
        }

        private fun setupCallTypeIndicator(item: CallLogItem) {
            val context = binding.root.context

            when (item.callLog.callType) {
                CALL_TYPE_INCOMING -> {
                    binding.callTypeIconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_incoming)
                    )
                    binding.callTypeIcon.setImageResource(R.drawable.ic_call_incoming)
                    binding.callType.text = "INCOMING"
                    binding.callTypeBadge.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_incoming)
                    )
                }
                CALL_TYPE_OUTGOING -> {
                    binding.callTypeIconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_outgoing)
                    )
                    binding.callTypeIcon.setImageResource(R.drawable.ic_call_outgoing)
                    binding.callType.text = "OUTGOING"
                    binding.callTypeBadge.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_outgoing)
                    )
                }
                CALL_TYPE_ANSWERED -> {
                    binding.callTypeIconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_answered)
                    )
                    binding.callTypeIcon.setImageResource(R.drawable.ic_call_incoming)
                    binding.callType.text = "ANSWERED"
                    binding.callTypeBadge.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_answered)
                    )
                }
                CALL_TYPE_MISSED -> {
                    binding.callTypeIconContainer.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_missed)
                    )
                    binding.callTypeIcon.setImageResource(R.drawable.miss_call)
                    binding.callType.text = "MISSED"
                    binding.callTypeBadge.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.call_missed)
                    )
                }
            }
        }

        private fun setupActionButton(item: CallLogItem, onCallAgainClickListener: (String) -> Unit) {
            binding.callAgainButton.setOnClickListener {
                animateCallButton()
                onCallAgainClickListener(item.callLog.phoneNumber)
            }
        }

        private fun formatPhoneNumber(phoneNumber: String): String {
            // Simple phone number formatting - could be enhanced based on locale
            return if (phoneNumber.length >= 10) {
                val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")
                when {
                    cleaned.length == 10 -> "${cleaned.substring(0, 3)}-${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
                    cleaned.length == 11 && cleaned.startsWith("1") -> "+1 ${cleaned.substring(1, 4)}-${cleaned.substring(4, 7)}-${cleaned.substring(7)}"
                    else -> phoneNumber
                }
            } else phoneNumber
        }

        private fun formatDateTime(timestamp: Long): String {
            return when {
                DateUtils.isToday(timestamp) -> {
                    "Today ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))}"
                }
                isYesterday(timestamp) -> {
                    "Yesterday ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))}"
                }
                isThisWeek(timestamp) -> {
                    SimpleDateFormat("EEEE h:mm a", Locale.getDefault()).format(Date(timestamp))
                }
                isThisYear(timestamp) -> {
                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
                }
                else -> {
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }

        private fun isYesterday(timestamp: Long): Boolean {
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val timestampCal = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }
            return yesterday.get(Calendar.DAY_OF_YEAR) == timestampCal.get(Calendar.DAY_OF_YEAR) &&
                    yesterday.get(Calendar.YEAR) == timestampCal.get(Calendar.YEAR)
        }

        private fun isThisWeek(timestamp: Long): Boolean {
            val now = Calendar.getInstance()
            val timestampCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val daysBetween = ((now.timeInMillis - timestamp) / (1000 * 60 * 60 * 24)).toInt()
            return daysBetween in 0..6
        }

        private fun isThisYear(timestamp: Long): Boolean {
            val now = Calendar.getInstance()
            val timestampCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            return now.get(Calendar.YEAR) == timestampCal.get(Calendar.YEAR)
        }

        private fun animateCallButton() {
            val scaleAnimation = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.callAgainButton, "scaleX", 1f, 0.9f, 1f),
                    ObjectAnimator.ofFloat(binding.callAgainButton, "scaleY", 1f, 0.9f, 1f)
                )
                duration = 150
            }
            scaleAnimation.start()
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
