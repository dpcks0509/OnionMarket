package pnu.cse.onionmarket.chat.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnu.cse.onionmarket.databinding.ItemChatDetailBinding

class ChatDetailAdapter(private val nickname: TextView)
    : ListAdapter<ChatDetailItem, ChatDetailAdapter.ViewHolder>(differ) {

    var chatDetailList = mutableListOf<ChatDetailItem>()

    inner class ViewHolder(private val binding: ItemChatDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatDetailItem) {
            Glide.with(binding.profileImage)
                .load(item.userProfile)
                .into(binding.profileImage)
            binding.message .text = item.message
            nickname.text = item.userName
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
        return chatDetailList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatDetailList[position])
    }
}