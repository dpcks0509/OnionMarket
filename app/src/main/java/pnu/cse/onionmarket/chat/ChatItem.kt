package pnu.cse.onionmarket.chat

import pnu.cse.onionmarket.chat.detail.ChatDetailItem

data class ChatItem(
    val chatRoomId: String? = null, // chatRoom의 고유 ID (랜덤)
    val otherUserId: String? = null, // 상대방ID
    val otherUserProfile: String? = null, // 상대방 프로필사진
    val otherUserName: String? = null, // 상대방 닉네임
    var lastMessage: String? = null, // 최근대화 메세지
    var unreadMessage: Int? = 0, // 안읽은 메세지 개수
    var lastMessageTime: Long? = null,
    var chats: HashMap<String, ChatDetailItem>? = null,
)