package pnu.cse.onionmarket.payment.waybill

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
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
import pnu.cse.onionmarket.chat.detail.ChatDetailItem
import pnu.cse.onionmarket.databinding.FragmentWaybillBinding

import pnu.cse.onionmarket.payment.transaction.TransactionAdapter
import pnu.cse.onionmarket.payment.transaction.TransactionItem
import pnu.cse.onionmarket.post.PostItem
import java.io.IOException
import java.util.*

class WaybillFragment : Fragment(R.layout.fragment_waybill) {
    private lateinit var binding: FragmentWaybillBinding
    private val args: WaybillFragmentArgs by navArgs()

    private lateinit var chatDetailAdapter: ChatDetailAdapter

    private val chatDetailItemList = mutableListOf<ChatDetailItem>()

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var otherUserToken: String = ""
    private var otherUserProfileImage: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var myUserProfileImage: String = ""
    private var transactionId: String = ""

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
        binding = FragmentWaybillBinding.bind(view)
        myUserId = Firebase.auth.currentUser?.uid!!

        chatDetailAdapter = ChatDetailAdapter()

        val itemList =
            listOf(
                "택배사", "CJ대한통운", "우체국택배", "한진택배", "롯데택배", "홈픽", "로젠택배", "GS25편의점택배", "CU 편의점택배",
                "경동택배", "대신택배", "일양로지스"
            )
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, itemList)
        binding.waybillCompany.adapter = adapter

        var companyPosition = 0

        binding.waybillCompany.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    companyPosition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.waybillTooltip.setOnClickListener {
            // 툴팁
            val balloon = Balloon.Builder(requireContext())
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText("배송지로 택배 배송 후 운송장번호를 등록해주세요.\n" +
                        "24시간 이내에 등록하지 않을시, 결제가 취소됩니다.")
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

            balloon.showAlignBottom(binding.waybillTooltip)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val userId = Firebase.auth.currentUser?.uid
        val postId = args.postId

        var postThumbnailImage = ""
        var postTitle = ""
        var postPrice = ""

        Firebase.database.reference.child("Posts").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(PostItem::class.java)
                    postThumbnailImage = post?.postThumbnailUrl!!
                    postTitle = post?.postTitle!!
                    postPrice = post?.postPrice!!
                    otherUserId = post?.buyerId!!
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
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        Firebase.database.reference.child("Transactions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.map {
                    val transaction = it.getValue(TransactionItem::class.java)
                    transaction ?: return
                    if(transaction.postId == postId) {
                        transactionId = transaction.transactionId!!
                        binding.name.setText(transaction.name)
                        binding.phone.setText(transaction.phone)
                        binding.address.setText(transaction.address)

                        if(!transaction.waybillNumber.isNullOrEmpty()) {
                            binding.submitButton.visibility = View.GONE
                            binding.waybillCompany.apply {
                                isClickable = false
                                isFocusable = false
                                isEnabled = false
                                setSelection(transaction.waybillCompanyPosition!!)
                            }

                            binding.waybillNumber.apply {
                                isClickable = false
                                isFocusable = false
                                setText(transaction.waybillNumber)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        Firebase.database.reference.child("Users").child(userId!!).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.userNickname ?: ""
                myUserProfileImage = myUserItem?.userProfileImage ?: ""

                getOtherUserData()
            }

        binding.submitButton.setOnClickListener {
            val company = itemList[companyPosition]
            if (companyPosition == 0 || binding.waybillNumber.text.isNullOrEmpty()) {
                Toast.makeText(context, "운송장 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val chatRoomDB =
                Firebase.database.reference.child("ChatRooms").child(userId!!).child(otherUserId)

            chatRoomDB.get().addOnSuccessListener {

                if (it.value != null) {
                    val chatRoom = it.getValue(ChatItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId!!


                } else {
                    chatRoomId = UUID.randomUUID().toString()
                }
                // 메세지 , 알림 보내기
                val message = "<운송장정보 등록 알림>\n" +
                        "택배사 : ${company}\n" +
                        "운송장 번호 : ${binding.waybillNumber.text}\n\n" +
                        " 택배 도착후 3일 이내에\n" +
                        " 구매확정 버튼을 누르지 않을시\n" +
                        " 자동으로 결제가 완료됩니다."

                val newChatItem = ChatDetailItem(
                    message = message,
                    userId = userId,
                    userProfile = myUserProfileImage
                )

                Firebase.database.reference.child("Chats").child(chatRoomId).push().apply {
                    newChatItem.chatId = key
                    setValue(newChatItem)
                }

                ChatDetailFragment.unreadMessage += 1

                chatDetailAdapter.submitList(chatDetailItemList.toMutableList())

                val lastMessageTime = System.currentTimeMillis()

                val newChatRoom = ChatItem(
                    chatRoomId = chatRoomId,
                    otherUserId = otherUserId,
                    otherUserProfile = otherUserProfileImage,
                    otherUserName = otherUserName,
                    lastMessage = message,
                    lastMessageTime = lastMessageTime,
                )
                chatRoomDB.setValue(newChatRoom)

                val updates: MutableMap<String, Any> = hashMapOf(
                    "ChatRooms/$otherUserId/$userId/lastMessage" to message,
                    "ChatRooms/$otherUserId/$userId/chatRoomId" to chatRoomId,
                    "ChatRooms/$otherUserId/$userId/otherUserId" to userId,
                    "ChatRooms/$otherUserId/$userId/otherUserName" to myUserName,
                    "ChatRooms/$otherUserId/$userId/otherUserProfile" to myUserProfileImage,
                    "ChatRooms/$otherUserId/$userId/unreadMessage" to ChatDetailFragment.unreadMessage,
                    "ChatRooms/$otherUserId/$userId/lastMessageTime" to lastMessageTime,
                    "Transactions/$transactionId/waybillCompanyPosition" to companyPosition,
                    "Transactions/$transactionId/waybillCompany" to company,
                    "Transactions/$transactionId/waybillNumber" to binding.waybillNumber.text.toString()

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

                val action = WaybillFragmentDirections.actionWaybillFragmentToChatDetailFragment(
                    chatRoomId = chatRoomId,
                    otherUserId = otherUserId
                )
                findNavController().navigate(action)
            }
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