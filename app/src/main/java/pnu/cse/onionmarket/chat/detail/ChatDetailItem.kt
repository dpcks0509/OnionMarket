package pnu.cse.onionmarket.chat.detail

data class ChatDetailItem(
    var chatId: String? = null,
    val userId: String? = null,
    val userProfile: Int? = null,
    val userName: String? = null,
    val message: String? = null
)