package pnu.cse.onionmarket.post

data class PostItem(
    val postId: String? = null,
    val createdAt: Long? = null,
    var postImagesUrl: List<String>? = null,
    var postThumbnailUrl: String? = null,
    var postTitle: String? = null,
    var postPrice: String? = null,
    var postContent: String? = null,
    var postStatus: Boolean? = null,
    val writerId: String? = null,
    var writerNickname: String? = null,
    var writerPhone: String? = null,
    var writerStar: Double? = null
)
