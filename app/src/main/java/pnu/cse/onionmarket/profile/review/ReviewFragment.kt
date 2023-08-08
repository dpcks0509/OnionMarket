package pnu.cse.onionmarket.profile.review

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentReviewBinding

class ReviewFragment: Fragment(R.layout.fragment_review) {
    private lateinit var binding: FragmentReviewBinding
    private lateinit var reviewAdapter: ReviewAdapter

    private val reviewList = mutableListOf<ReviewItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewBinding.bind(view)

        reviewAdapter = ReviewAdapter()
        reviewAdapter.reviewList = reviewList

        binding.reviewRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewAdapter
        }

        // 데이터 예시
        with(reviewList) {
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
            add(ReviewItem("1", "1", R.drawable.app_logo, "번데기껍질", "좋은거래였어요", 4.5))
        }
    }
}