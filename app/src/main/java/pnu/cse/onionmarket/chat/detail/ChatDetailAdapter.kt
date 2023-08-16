package pnu.cse.onionmarket.chat.detail

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnu.cse.onionmarket.UserItem
import pnu.cse.onionmarket.databinding.ItemChatDetailBinding

class ChatDetailAdapter()
    : ListAdapter<ChatDetailItem, ChatDetailAdapter.ViewHolder>(differ) {

    var otherUserItem: UserItem? = null

    inner class ViewHolder(private val binding: ItemChatDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatDetailItem) {
            Glide.with(binding.profileImage)
                .load(item.userProfile)
                .into(binding.profileImage)
            binding.message .text = item.message

            if(item.userId == otherUserItem?.userId) {
                binding.profileImage.isVisible = true
                binding.chatItem.gravity = Gravity.START
            } else {
                binding.profileImage.isVisible = false
                binding.chatItem.gravity = Gravity.END
            }
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatDetailItem>() {
            override fun areItemsTheSame(oldItem: ChatDetailItem, newItem: ChatDetailItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(oldItem: ChatDetailItem, newItem: ChatDetailItem): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemChatDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}