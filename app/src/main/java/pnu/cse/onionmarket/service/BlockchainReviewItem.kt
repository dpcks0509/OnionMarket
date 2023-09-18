package pnu.cse.onionmarket.service

data class BlockchainReviewItem(
    var userId: String? = null,
    var reviewId: String? = null,
    var reviewStar: Double? = null,
    var reviewText: String? = null,
    var writerNickname: String? = null,
    var createdAt: Long? = null
)
