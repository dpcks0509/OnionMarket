package pnu.cse.onionmarket.profile.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnu.cse.onionmarket.databinding.ItemReviewBinding

class ReviewAdapter()
    : ListAdapter<ReviewItem, ReviewAdapter.ViewHolder>(differ) {

    var reviewList = mutableListOf<ReviewItem>()

    inner class ViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewItem) {
            Glide.with(binding.profileImageView)
                .load(item.userProfile)
                .into(binding.profileImageView)

            binding.nicknameTextView.text = item.userName
            binding.reviewTextView.text = item.reviewText
            binding.starNumber.text = item.reviewStar.toString()
        }
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<ReviewItem>() {
            override fun areItemsTheSame(oldItem: ReviewItem, newItem: ReviewItem): Boolean {
                return oldItem.reviewId == newItem.reviewId
            }

            override fun areContentsTheSame(oldItem: ReviewItem, newItem: ReviewItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }
}