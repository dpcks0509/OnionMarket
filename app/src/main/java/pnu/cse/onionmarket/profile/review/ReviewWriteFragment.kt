package pnu.cse.onionmarket.profile.review

import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentReviewWriteBinding
import java.util.UUID

class ReviewWriteFragment : Fragment(R.layout.fragment_review_write) {
    private lateinit var binding: FragmentReviewWriteBinding
    private val args: ReviewWriteFragmentArgs by navArgs()

    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<ReviewItem>()

    private lateinit var postId: String

    override fun onResume() {
        super.onResume()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(true)
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewWriteBinding.bind(view)

        val reviewId = UUID.randomUUID().toString()
        val myUserId = Firebase.auth.currentUser?.uid
        val profileUserId = args.profileUserId
        postId = args.postId
        var reviewStar = 0.0

        binding.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating < 0.5f) {
                    ratingBar.rating = 0.5f
                }
                reviewStar = rating.toDouble()
            }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            if (reviewStar == 0.0 || binding.reviewText.text.toString().isNullOrEmpty()) {
                Toast.makeText(context, "리뷰 작성에 필요한 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val update: MutableMap<String, Any> = hashMapOf(
                "Posts/$postId/reviewWrite" to true
            )

            Firebase.database.reference.updateChildren(update)

            Firebase.database.reference.child("Users").child(myUserId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userName = snapshot.child("userNickname").getValue(String::class.java)
                        var userProfileImage =
                            snapshot.child("userProfileImage").getValue(String::class.java)

                        val review = ReviewItem(
                            reviewId = reviewId,
                            createdAt = System.currentTimeMillis(),
                            userId = profileUserId,
                            userProfile = userProfileImage,
                            userName = userName,
                            reviewText = binding.reviewText.text.toString(),
                            reviewStar = reviewStar
                        )

                        Firebase.database.reference.child("Reviews").child(reviewId)
                            .setValue(review)
                            .addOnSuccessListener {

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

                                            Firebase.database.reference.child("Reviews")
                                                .removeEventListener(this)


                                            val update: MutableMap<String, Any> = hashMapOf(
                                                "Users/$profileUserId/userStar" to userReviewStar
                                            )

                                            Firebase.database.reference.updateChildren(update)


                                        }

                                        override fun onCancelled(error: DatabaseError) {}

                                    })
                            }


                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            findNavController().popBackStack()
        }

        val reviewsRef = Firebase.database.reference.child("Reviews")
        val reviewsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateStarRating(profileUserId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateStarRating(profileUserId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                updateStarRating(profileUserId)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Not used in this case
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        }

        reviewsRef.addChildEventListener(reviewsListener)
    }

    private fun updateStarRating(profileUserId: String) {
        var userReviewSum = 0.0
        var userReviewNumber = 0
        var userReviewStar = 0.0

        val reviewsRef = Firebase.database.reference.child("Reviews")
        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userReviewSum = 0.0
                userReviewNumber = 0

                snapshot.children.map { reviewSnapshot ->
                    val review = reviewSnapshot.getValue(ReviewItem::class.java)
                    review ?: return@map

                    if (review.userId == profileUserId) {
                        userReviewSum += review.reviewStar!!
                        userReviewNumber += 1
                    }
                }

                if (userReviewNumber != 0) {
                    userReviewStar = userReviewSum / userReviewNumber
                    userReviewStar = String.format("%.1f", userReviewStar).toDouble()
                } else {
                    userReviewStar = 0.0
                }

                val updateUserStar: MutableMap<String, Any> = hashMapOf(
                    "Users/$profileUserId/userStar" to userReviewStar,
                )

                Firebase.database.reference.updateChildren(updateUserStar)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

}