package pnu.cse.onionmarket.payment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skydoves.balloon.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.UserItem
import pnu.cse.onionmarket.chat.ChatItem
import pnu.cse.onionmarket.chat.detail.ChatDetailAdapter
import pnu.cse.onionmarket.chat.detail.ChatDetailFragment
import pnu.cse.onionmarket.chat.detail.ChatDetailFragment.Companion.unreadMessage
import pnu.cse.onionmarket.chat.detail.ChatDetailItem
import pnu.cse.onionmarket.databinding.FragmentSafePaymentBinding
import pnu.cse.onionmarket.payment.transaction.TransactionAdapter
import pnu.cse.onionmarket.payment.transaction.TransactionItem
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.post.detail.PostDetailFragmentDirections
import pnu.cse.onionmarket.wallet.WalletFragmentDirections
import pnu.cse.onionmarket.wallet.WalletItem
import java.io.IOException
import java.util.*


class SafePaymentFragment : Fragment(R.layout.fragment_safe_payment) {
    private lateinit var binding: FragmentSafePaymentBinding
    private val args: SafePaymentFragmentArgs by navArgs()

    private lateinit var chatDetailAdapter: ChatDetailAdapter
    private lateinit var transactionAdapter: TransactionAdapter

    private var transactionList = mutableListOf<TransactionItem>()

    private val chatDetailItemList = mutableListOf<ChatDetailItem>()

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var otherUserToken: String = ""
    private var otherUserProfileImage: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var myUserProfileImage: String = ""

    override fun onResume() {
        super.onResume()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(true)
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSafePaymentBinding.bind(view)
        myUserId = Firebase.auth.currentUser?.uid!!

        chatDetailAdapter = ChatDetailAdapter()

        transactionAdapter = TransactionAdapter { item ->
            val action = WalletFragmentDirections.actionWalletFragmentToPostDetailFragment(
                postId = item.postId!!,
                writerId = item.sellerId!!
            )
            findNavController().navigate(action)
        }



        val userId = Firebase.auth.currentUser?.uid
        val postId = args.postId
        val writerId = args.writerId

        var postThumbnailImage = ""
        var postTitle = ""
        var postPrice = ""
        otherUserId = writerId

        Firebase.database.reference.child("Posts").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(PostItem::class.java)
                    postThumbnailImage = post?.postThumbnailUrl!!
                    postTitle = post?.postTitle!!
                    postPrice = post?.postPrice!!
                    Glide.with(binding.postThumbnail)
                        .load(postThumbnailImage)
                        .into(binding.postThumbnail)

                    binding.postTitle.text = postTitle

                    var priceWithoutCommas = postPrice
                    var formattedPrice = StringBuilder()
                    var commaCounter = 0

                    for (i in priceWithoutCommas.length - 1 downTo 0) {
                        formattedPrice.append(priceWithoutCommas[i])
                        commaCounter++

                        if (commaCounter == 3 && i > 0) {
                            formattedPrice.append(",")
                            commaCounter = 0
                        }
                    }
                    formattedPrice.reverse()
                    binding.postPrice.text = "${formattedPrice}원"

                    val postPrice = post?.postPrice?.toInt()
                    val chargePrice = postPrice?.floorDiv(100)
                    val totalPrice = postPrice?.plus(chargePrice!!)

                    priceWithoutCommas = postPrice.toString()
                    formattedPrice = StringBuilder()
                    commaCounter = 0

                    for (i in priceWithoutCommas.length - 1 downTo 0) {
                        formattedPrice.append(priceWithoutCommas[i])
                        commaCounter++

                        if (commaCounter == 3 && i > 0) {
                            formattedPrice.append(",")
                            commaCounter = 0
                        }
                    }
                    formattedPrice.reverse()
                    binding.detailPostPrice.text = "${formattedPrice}원"

                    priceWithoutCommas = chargePrice.toString()
                    formattedPrice = StringBuilder()
                    commaCounter = 0

                    for (i in priceWithoutCommas.length - 1 downTo 0) {
                        formattedPrice.append(priceWithoutCommas[i])
                        commaCounter++

                        if (commaCounter == 3 && i > 0) {
                            formattedPrice.append(",")
                            commaCounter = 0
                        }
                    }
                    formattedPrice.reverse()
                    binding.chargePrice.text = "${formattedPrice}원"

                    priceWithoutCommas = totalPrice.toString()
                    formattedPrice = StringBuilder()
                    commaCounter = 0

                    for (i in priceWithoutCommas.length - 1 downTo 0) {
                        formattedPrice.append(priceWithoutCommas[i])
                        commaCounter++

                        if (commaCounter == 3 && i > 0) {
                            formattedPrice.append(",")
                            commaCounter = 0
                        }
                    }
                    formattedPrice.reverse()
                    binding.totalPrice.text = "${formattedPrice}원"
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        Firebase.database.reference.child("Wallets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val wallet = it.getValue(WalletItem::class.java)
                        wallet ?: return
                        if (wallet.userId == userId) {
                            if (!wallet.walletImage.isNullOrEmpty())
                                Glide.with(binding.walletImage)
                                    .load(wallet.walletImage)
                                    .into(binding.walletImage)
                            binding.walletName.text = wallet.walletName

                            val postPrice = wallet.walletMoney
                            val chargePrice = postPrice?.floorDiv(100)
                            val totalPrice = postPrice?.plus(chargePrice!!)

                            var priceWithoutCommas = postPrice.toString()
                            var formattedPrice = StringBuilder()
                            var commaCounter = 0

                            for (i in priceWithoutCommas.length - 1 downTo 0) {
                                formattedPrice.append(priceWithoutCommas[i])
                                commaCounter++

                                if (commaCounter == 3 && i > 0) {
                                    formattedPrice.append(",")
                                    commaCounter = 0
                                }
                            }
                            formattedPrice.reverse()
                            binding.walletMoney.text = "${formattedPrice}원"
                            return@forEach
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        Firebase.database.reference.child("Users").child(userId!!).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.userNickname ?: ""
                myUserProfileImage = myUserItem?.userProfileImage ?: ""

                getOtherUserData()
            }


        binding.submitButton.setOnClickListener {
            val walletMoney =
                binding.walletMoney.text.toString().replace(",", "").replace("원", "").toDouble()
            val totalMoney =
                binding.totalPrice.text.toString().replace(",", "").replace("원", "").toDouble()

            if (binding.name.text.isNullOrEmpty() || binding.phone.text.isNullOrEmpty() || binding.address.text.isNullOrEmpty()) {
                Toast.makeText(context, "배송지 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (walletMoney < totalMoney) {
                Toast.makeText(context, "거래에 필요한 지갑 잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val chatRoomDB =
                Firebase.database.reference.child("ChatRooms").child(userId!!).child(writerId)

            chatRoomDB.get().addOnSuccessListener {
                if (it.value != null) {
                    val chatRoom = it.getValue(ChatItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId!!


                } else {
                    chatRoomId = UUID.randomUUID().toString()
                }
                // 메세지 , 알림 보내기
                val message = "<안전결제 알림>\n" +
                        "상품명 : [${binding.postTitle.text}]\n" +
                        "상품가격 : ${binding.postPrice.text}원\n\n" +
                        "-배송지-\n" +
                        "이름 : ${binding.name.text}\n" +
                        "연락처 : ${binding.phone.text}\n" +
                        "주소 : ${binding.address.text}\n\n" +
                        " 배송지로 택배 배송 후 운송장번호를 등록해주세요.\n" +
                        " 24시간 이내에 등록하지 않을시\n" +
                        " 안전결제가 취소됩니다."

                val newChatItem = ChatDetailItem(
                    message = message,
                    userId = userId,
                    userProfile = myUserProfileImage
                )

                Firebase.database.reference.child("Chats").child(chatRoomId).push().apply {
                    newChatItem.chatId = key
                    setValue(newChatItem)
                }

                unreadMessage += 1

                chatDetailAdapter.submitList(chatDetailItemList.toMutableList())

                val lastMessageTime = System.currentTimeMillis()

                val newChatRoom = ChatItem(
                    chatRoomId = chatRoomId,
                    otherUserId = otherUserId,
                    otherUserProfile = otherUserProfileImage,
                    otherUserName = otherUserName,
                    lastMessage = message,
                    lastMessageTime = lastMessageTime
                )
                chatRoomDB.setValue(newChatRoom)

                val updates: MutableMap<String, Any> = hashMapOf(
                    "ChatRooms/$otherUserId/$userId/lastMessage" to message,
                    "ChatRooms/$otherUserId/$userId/chatRoomId" to chatRoomId,
                    "ChatRooms/$otherUserId/$userId/otherUserId" to userId,
                    "ChatRooms/$otherUserId/$userId/otherUserName" to myUserName,
                    "ChatRooms/$otherUserId/$userId/otherUserProfile" to myUserProfileImage,
                    "ChatRooms/$otherUserId/$userId/unreadMessage" to unreadMessage,
                    "ChatRooms/$otherUserId/$userId/lastMessageTime" to lastMessageTime
                )

                Firebase.database.reference.updateChildren(updates)

                val client = OkHttpClient()
                val root = JSONObject()
                val notification = JSONObject()
                notification.put("title", myUserName)
                notification.put("body", message)
                notification.put("chatRoomId", chatRoomId)
                notification.put("otherUserId", userId)

                root.put("to", otherUserToken)
                root.put("priority", "high")
                root.put("data", notification)

                val requestBody =
                    root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request =
                    Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                        .header("Authorization", "key=${getString(R.string.fcm_server_key)}")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                    }

                })

                val transactionId = UUID.randomUUID().toString()
                val createdAt = System.currentTimeMillis()
                val transaction = TransactionItem(
                    transactionId = transactionId,
                    createdAt = createdAt,
                    postId = postId,
                    sellerId = writerId,
                    buyerId = myUserId,
                    postThumbnailImage = postThumbnailImage,
                    postTitle = postTitle,
                    postPrice = postPrice,
                    name = binding.name.text.toString(),
                    phone = binding.phone.text.toString(),
                    address = binding.address.text.toString()
                )

                Firebase.database.reference.child("Transactions").child(transactionId).setValue(transaction)
                    .addOnSuccessListener {
                        val updates: MutableMap<String, Any> = hashMapOf(
                            "Posts/$postId/postStatus" to false,
                            "Posts/$postId/buyerId" to myUserId
                        )
                        Firebase.database.reference.updateChildren(updates)
                    }

                val action =
                    SafePaymentFragmentDirections.actionSafePaymentFragmentToChatDetailFragment(
                        chatRoomId = chatRoomId,
                        otherUserId = writerId
                    )
                findNavController().navigate(action)
            }
        }

        binding.postInfo.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chargePriceTooltip.setOnClickListener {
            // 툴팁
            val balloon = Balloon.Builder(requireContext())
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText("발생가능한 최대 수수료는 1%이며,\n거래완료시 수수료 차액은 결제되지 않습니다.")
                .setTextColorResource(R.color.white)
                .setTextSize(15f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowSize(10)
                .setArrowPosition(0.5f)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBackgroundColorResource(R.color.black)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

            balloon.showAlignBottom(binding.chargePriceTooltip)
        }

    }

    private fun getChatData() {

        Firebase.database.reference.child("Chats").child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatDetailItem = snapshot.getValue(ChatDetailItem::class.java)
                    chatDetailItem ?: return

                    chatDetailItemList.add(chatDetailItem)
                    chatDetailAdapter.submitList(chatDetailItemList.toMutableList())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child("Users").child(otherUserId!!).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                chatDetailAdapter.otherUserItem = otherUserItem
                otherUserName = otherUserItem?.userNickname.toString()
                otherUserToken = otherUserItem?.userToken.orEmpty()
                otherUserProfileImage = otherUserItem?.userProfileImage.orEmpty()
                getChatData()
            }
    }
}



