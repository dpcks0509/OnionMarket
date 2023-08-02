package pnu.cse.onionmarket.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnu.cse.onionmarket.databinding.ItemChatBinding

class ChatAdapter(private val onClick: (ChatItem) -> Unit) :
    ListAdapter<ChatItem, ChatAdapter.ViewHolder>(differ) {

    var chatList = mutableListOf<ChatItem>()

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            Glide.with(binding.profileImageView)
                .load(item.otherUserProfile)
                .into(binding.profileImageView)

            binding.nicknameTextView.text = item.otherUserName
            binding.lastMessageTextView.text = item.lastMessage
            binding.unreadMessage.text = item.unreadMessageNumber.toString()

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatRoomId == newItem.chatRoomId
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }
}