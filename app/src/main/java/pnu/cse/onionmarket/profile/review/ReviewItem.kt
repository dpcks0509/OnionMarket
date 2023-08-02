package pnu.cse.onionmarket.profile.review

data class ReviewItem(
    var reviewId: String? = null,
    val userId: String? = null,
    val userProfile: Int? = null,
    val userName: String? = null,
    val reviewText: String? = null,
    val reviewStar: Double? = null
)
