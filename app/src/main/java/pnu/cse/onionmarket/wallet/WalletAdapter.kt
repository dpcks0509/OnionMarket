package pnu.cse.onionmarket.wallet

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.ItemWalletBinding

class WalletAdapter() : ListAdapter<WalletItem, WalletAdapter.ViewHolder>(differ) {

    var walletList = mutableListOf<WalletItem>()

    inner class ViewHolder(private val binding: ItemWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletItem) {
            binding.walletName.text = item.walletName

            val priceWithoutCommas = item.walletMoney.toString()
            val formattedPrice = StringBuilder()
            var commaCounter = 0

            for (i in priceWithoutCommas.length - 1 downTo 0) {
                formattedPrice.append(priceWithoutCommas[i])
                commaCounter++

                if (commaCounter == 3 && i > 0) {
                    formattedPrice.append(",")
                    commaCounter = 0
                }
            }
            formattedPrice.reverse()

            binding.walletMoney.text = "$formattedPrice 원"

            binding.walletEditButton.setOnClickListener {
                var popupMenu = PopupMenu(itemView.context, it)

                popupMenu.menuInflater.inflate(R.menu.menu_wallet, popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.wallet_edit_button -> {
                            itemView.findNavController()
                                .navigate(R.id.action_walletFragment_to_walletAddFragment)
                        }
                        R.id.wallet_delete_button -> {
                            val builder = AlertDialog.Builder(itemView.context)
                            builder
                                .setTitle("지갑삭제")
                                .setMessage("정말로 지갑을 삭제하시겠습니까?")
                                .setPositiveButton("삭제",
                                    DialogInterface.OnClickListener { dialog, id ->
                                        walletList.remove(item)
                                        notifyItemRemoved(position)
                                    })
                                .setNegativeButton("취소",
                                    DialogInterface.OnClickListener { dialog, id -> })

                            builder.create()
                            builder.show()
                        }

                    }
                    false
                }
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