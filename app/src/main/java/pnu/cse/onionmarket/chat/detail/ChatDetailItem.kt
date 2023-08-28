package pnu.cse.onionmarket.chat.detail

data class ChatDetailItem(
    var chatId: String? = null, // chat의 고유 ID (랜덤)
    val userId: String? = null, // 채팅보낸 user의 ID
    val userProfile: String? = null, // 채팅보낸 user의 프로필사진
    val userName: String? = null, // 채팅보낸 user의 닉네임
    val message: String? = null, // 보낸 메세지
)