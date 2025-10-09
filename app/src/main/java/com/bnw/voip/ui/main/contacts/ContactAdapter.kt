package com.bnw.voip.ui.main.contacts

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bnw.voip.data.entity.Contact
import com.bnw.voip.databinding.ItemContactBinding
import java.util.*

class ContactAdapter(
    private val onCallClickListener: (String) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.contactName.text = contact.name
            binding.contactNumber.text = contact.phoneNumbers.firstOrNull()
            binding.contactInitial.text = contact.name.first().toString()

            val randomColor = getRandomColor()
            val background = binding.contactInitial.background as GradientDrawable
            background.setColor(randomColor)

            binding.callButton.setOnClickListener {
                if (contact.phoneNumbers.size == 1) {
                    onCallClickListener(contact.phoneNumbers[0])
                } else {
                    val builder = AlertDialog.Builder(binding.root.context)
                    builder.setTitle("Choose a number")
                    builder.setItems(contact.phoneNumbers.toTypedArray()) { _, which ->
                        onCallClickListener(contact.phoneNumbers[which])
                    }
                    builder.show()
                }
            }
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
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
