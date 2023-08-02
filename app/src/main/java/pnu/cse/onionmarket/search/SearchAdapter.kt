package pnu.cse.onionmarket.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pnu.cse.onionmarket.databinding.ItemSearchBinding

class SearchAdapter(private val onClick: (SearchItem) -> Unit) :
    ListAdapter<SearchItem, SearchAdapter.ViewHolder>(differ) {

    inner class ViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem) {
            binding.searchedText.text = item.searchedText

            binding.searchedText.setOnClickListener {
                onClick(item)
            }

            binding.removeButton.setOnClickListener {
                removeItem(item)
            }
        }
    }

    companion object {
        private val searchList: MutableList<SearchItem> = mutableListOf()

        val differ = object : DiffUtil.ItemCallback<SearchItem>() {
            override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
                return oldItem.searchId == newItem.searchId
            }

            override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchList[position])
    }

    private fun removeItem(item: SearchItem) {
        searchList.remove(item)
        notifyDataSetChanged()
    }

    fun addItem(item: SearchItem) {
        searchList.add(item)
        notifyDataSetChanged()
    }
}
