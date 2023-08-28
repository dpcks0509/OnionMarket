package pnu.cse.onionmarket

data class UserItem(
    val userId: String? = null, // user의 고유 ID (랜덤)
    val userNickname: String? = null, // user의 닉네임
    val userPhone: String? = null, // user의 핸드폰번호
    val userStar: Double? = 0.0, // user의 후기 평점 (초기값 0)
    val userToken: String? = null, // user의 fcm token(채팅 알림 받기위한 용도)
    var userProfileImage: String? = null
)
