package com.bnw.voip.ui.main.contacts

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bnw.voip.R
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.databinding.ItemContactBinding

class ContactAdapter(
    private val onCallClickListener: (String) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    // Modern color palette for avatars
    private val avatarColors = arrayOf(
        R.color.contacts_avatar_1,
        R.color.contacts_avatar_2,
        R.color.contacts_avatar_3,
        R.color.contacts_avatar_4,
        R.color.contacts_avatar_5,
        R.color.contacts_avatar_6,
        R.color.contacts_avatar_7
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(contact: Contact) {
            setupContactInfo(contact)
            setupContactAvatar(contact)
            setupMultipleNumbersIndicator(contact)
            setupCallButton(contact)
        }

        private fun setupContactInfo(contact: Contact) {
            binding.contactName.text = contact.name
            binding.contactNumber.text = formatPhoneNumber(contact.phoneNumbers.firstOrNull() ?: "")
        }

        private fun setupContactAvatar(contact: Contact) {
            if (contact.photoUri != null) {
                // Show contact photo
                binding.contactPhoto.visibility = View.VISIBLE
                binding.contactInitial.visibility = View.GONE
                binding.contactPhoto.setImageURI(android.net.Uri.parse(contact.photoUri))
            } else {
                // Show initial with consistent color
                binding.contactPhoto.visibility = View.GONE
                binding.contactInitial.visibility = View.VISIBLE
                binding.contactInitial.text = contact.name.first().toString().uppercase()
                
                // Use consistent color based on first letter
                val colorIndex = (contact.name.first().code) % avatarColors.size
                val avatarColor = ContextCompat.getColor(binding.root.context, avatarColors[colorIndex])
                binding.contactPhotoContainer.setCardBackgroundColor(avatarColor)
            }
        }

        private fun setupMultipleNumbersIndicator(contact: Contact) {
            if (contact.phoneNumbers.size > 1) {
                binding.multipleNumbersIndicator.visibility = View.VISIBLE
                binding.multipleNumbersText.text = "${contact.phoneNumbers.size} numbers"
            } else {
                binding.multipleNumbersIndicator.visibility = View.GONE
            }
        }

        private fun setupCallButton(contact: Contact) {
            binding.callButton.setOnClickListener {
                animateCallButton()
                
                if (contact.phoneNumbers.size == 1) {
                    onCallClickListener(contact.phoneNumbers[0])
                } else {
                    showNumberSelectionDialog(contact)
                }
            }
        }

        private fun showNumberSelectionDialog(contact: Contact) {
            val context = binding.root.context
            val formattedNumbers = contact.phoneNumbers.map { formatPhoneNumber(it) }.toTypedArray()
            
            AlertDialog.Builder(context)
                .setTitle("Choose a number")
                .setItems(formattedNumbers) { _, which ->
                    onCallClickListener(contact.phoneNumbers[which])
                }
                .show()
        }

        private fun formatPhoneNumber(phoneNumber: String): String {
            // Simple phone number formatting
            return if (phoneNumber.length >= 10) {
                val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")
                when {
                    cleaned.length == 10 -> "${cleaned.substring(0, 3)}-${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
                    cleaned.length == 11 && cleaned.startsWith("1") -> "+1 ${cleaned.substring(1, 4)}-${cleaned.substring(4, 7)}-${cleaned.substring(7)}"
                    else -> phoneNumber
                }
            } else phoneNumber
        }

        private fun animateCallButton() {
            val scaleAnimation = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.callButton, "scaleX", 1f, 0.9f, 1f),
                    ObjectAnimator.ofFloat(binding.callButton, "scaleY", 1f, 0.9f, 1f)
                )
                duration = 150
            }
            scaleAnimation.start()
        }
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}