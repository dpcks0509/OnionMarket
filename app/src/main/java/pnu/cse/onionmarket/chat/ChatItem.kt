package pnu.cse.onionmarket.chat

data class ChatItem(
    val chatRoomId: String? = null,
    val otherUserId: String? = null,
    val otherUserProfile: Int? = null,
    val otherUserName: String? = null,
    val lastMessage: String? = null,
    val lastTime: String? = null,
    val unreadMessageNumber: Int? = null
)