package pnu.cse.onionmarket.post

import android.net.Uri

data class PostItem(
    val postId: String? = null, // post의 고유 ID(랜덤)
    val createdAt: Long? = null, // post의 생성시간,
    var postImagesUri: List<String>? = null,
    var postImagesUrl: List<String>? = null, // post의 이미지들(최대 10장)
    var postThumbnailUrl: String? = null, // post의 썸네일 이미지
    var postTitle: String? = null, // post의 제목
    var postPrice: String? = null, // post의 가격
    var postContent: String? = null, // post의 상세내용
    var postStatus: Boolean? = null, // post의 상태 (true -> 판매중, false -> 판매완료)
    val writerId: String? = null, // post의 작성자ID
    var writerNickname: String? = null, // post의 작성자 닉네임
    var writerPhone: String? = null, // post의 작성자 핸드폰 번호
    var writerStar: Double? = null, // post의 작성자 후기 별점
    var writerProfileImage: String? = null,
    var buyerId: String? = null,
    var reviewWrite: Boolean? = null,
)
