package pnu.cse.onionmarket.profile.review

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentReviewBinding
import pnu.cse.onionmarket.profile.selling.SellingFragment

class
ReviewFragment: Fragment(R.layout.fragment_review) {
    private lateinit var binding: FragmentReviewBinding
    private lateinit var reviewAdapter: ReviewAdapter

    private var reviewList = mutableListOf<ReviewItem>()

    private var profileUserId: String? = null

    companion object {
        fun newInstance(profileUserId: String?): ReviewFragment {
            val fragment = ReviewFragment()
            val args = Bundle()
            args.putString("profileUserId", profileUserId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewBinding.bind(view)

        arguments?.let {
            // Retrieve the writerId from the arguments bundle
            profileUserId = it.getString("profileUserId") ?: Firebase.auth.currentUser?.uid
        }

        reviewAdapter = ReviewAdapter()

        binding.reviewRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = reviewAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        Firebase.database.reference.child("Reviews")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewList = mutableListOf()

                    snapshot.children.map {
                        val review = it.getValue(ReviewItem::class.java)
                        review ?: return
                        if(review.userId == profileUserId)
                            reviewList.add(review)
                    }
                    reviewList.sortByDescending { it.createdAt }
                    reviewAdapter.submitList(reviewList)

                    if(reviewAdapter.currentList.isEmpty()) {
                        binding.noReview.visibility = View.VISIBLE
                    } else {
                        binding.noReview.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        var userReviewSum = 0.0
        var userReviewNumber = 0
        var userReviewStar = 0.0

        // users star 업데이트
        Firebase.database.reference.child("Reviews")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userReviewSum = 0.0
                    userReviewNumber = 0
                    userReviewStar = 0.0

                    snapshot.children.map {
                        val review = it.getValue(ReviewItem::class.java)
                        review ?: return

                        if (review.userId == profileUserId) {
                            userReviewSum += review.reviewStar!!
                            userReviewNumber += 1
                        }
                    }
                    if (userReviewNumber != 0) {
                        userReviewStar = (userReviewSum / userReviewNumber)
                        userReviewStar =
                            String.format("%.1f", userReviewStar).toDouble()
                    }

                    val update: MutableMap<String, Any> = hashMapOf(
                        "Users/$profileUserId/userStar" to userReviewStar
                    )

                    Firebase.database.reference.updateChildren(update)


                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}