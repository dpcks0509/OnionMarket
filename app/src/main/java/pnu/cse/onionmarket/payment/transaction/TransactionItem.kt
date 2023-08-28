package pnu.cse.onionmarket.payment.transaction

data class TransactionItem(
    var transactionId: String? = null,
    var createdAt: Long? = null,
    var postId: String? = null,
    var sellerId: String? = null,
    var buyerId: String? = null,
    var postThumbnailImage: String? = null,
    var postTitle: String? = null,
    var postPrice: String? = null,
    var name: String? = null,
    var phone: String? = null,
    var address: String? = null
)