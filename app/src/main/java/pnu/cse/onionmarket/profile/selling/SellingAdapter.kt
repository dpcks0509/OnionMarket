package pnu.cse.onionmarket.profile.selling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.ItemPostBinding
import pnu.cse.onionmarket.post.PostItem

class SellingAdapter(private val onClick: (PostItem) -> Unit) : ListAdapter<PostItem, SellingAdapter.ViewHolder>(
    differ
) {

    var sellingList = mutableListOf<PostItem>()

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostItem) {

            Glide.with(binding.postThumbnail)
                .load(item.postThumbnailUrl)
                .into(binding.postThumbnail)

            if(item.postStatus == true) {
                binding.postStatus.apply {
                    text = "판매중"
                    backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.main_color)
                }
            } else {
                binding.postStatus.apply {
                    text = "판매완료"
                    backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.gray)
                }
            }

            binding.postTitle.text = item.postTitle
            binding.postPrice.text = item.postPrice

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<PostItem>() {
            override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
                return oldItem.postId == newItem.postId
            }

            override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return sellingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sellingList[position])
    }
}