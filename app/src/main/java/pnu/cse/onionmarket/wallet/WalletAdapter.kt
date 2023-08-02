package pnu.cse.onionmarket.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pnu.cse.onionmarket.databinding.ItemWalletBinding

class WalletAdapter()
    : ListAdapter<WalletItem, WalletAdapter.ViewHolder>(differ) {

    var walletList = mutableListOf<WalletItem>()

    inner class ViewHolder(private val binding: ItemWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletItem) {

            binding.walletName.text = item.walletName
            binding.walletMoney.text = item.walletMoney.toString() + " 원"

            binding.deleteButton.setOnClickListener {
                walletList.remove(item)
                notifyItemRemoved(position)
            }

        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<WalletItem>() {
            override fun areItemsTheSame(oldItem: WalletItem, newItem: WalletItem): Boolean {
                return oldItem.walletAddress == newItem.walletAddress
            }

            override fun areContentsTheSame(oldItem: WalletItem, newItem: WalletItem): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWalletBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return walletList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walletList[position])
    }
}