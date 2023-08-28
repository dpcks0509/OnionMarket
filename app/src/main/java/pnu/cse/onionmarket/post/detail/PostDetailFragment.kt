package pnu.cse.onionmarket.post.detail

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import pnu.cse.onionmarket.LoginActivity
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.chat.ChatItem
import pnu.cse.onionmarket.databinding.FragmentPostDetailBinding
import pnu.cse.onionmarket.home.HomeFragmentDirections
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.profile.ProfileFragmentDirections
import pnu.cse.onionmarket.wallet.WalletItem
import java.util.UUID

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {
    private lateinit var binding: FragmentPostDetailBinding
    private val args: PostDetailFragmentArgs by navArgs()

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
        binding = FragmentPostDetailBinding.bind(view)

        val writerId = args.writerId
        val postId = args.postId
        val userId = Firebase.auth.currentUser?.uid



        if (userId == writerId) {
            binding.editButton.visibility = View.VISIBLE
            binding.safePaymentButton.visibility = View.GONE

            binding.chatButton.text = "나의채팅"
            binding.chatButton.setOnClickListener {
                findNavController().navigate(R.id.action_postDetailFragment_to_chatFragment)
            }
        } else {
            binding.editButton.visibility = View.INVISIBLE
            binding.safePaymentButton.visibility = View.VISIBLE

            var walletExist = false
            Firebase.database.reference.child("Wallets").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    snapshot.children.forEach {
                        val wallet = it.getValue(WalletItem::class.java)
                        wallet ?: return
                        if(wallet.userId == userId) {
                            walletExist = true
                            return@forEach
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

            binding.safePaymentButton.setOnClickListener {

                if(!walletExist) {
                    Toast.makeText(context,"안전결제에 필요한\n전자지갑을 먼저 등록해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val action =
                    PostDetailFragmentDirections.actionPostDetailFragmentToSafePaymentFragment(
                        postId = postId,
                        writerId = writerId
                    )
                findNavController().navigate(action)
            }

            binding.chatButton.text = "채팅하기"
            binding.chatButton.setOnClickListener {

                val chatRoomDB =
                    Firebase.database.reference.child("ChatRooms").child(userId!!).child(writerId)
                var chatRoomId = ""

                chatRoomDB.get().addOnSuccessListener {

                    if (it.value != null) {
                        val chatRoom = it.getValue(ChatItem::class.java)
                        chatRoomId = chatRoom?.chatRoomId!!

                        val action =
                            PostDetailFragmentDirections.actionPostDetailFragmentToChatDetailFragment(
                                chatRoomId = chatRoomId,
                                otherUserId = writerId
                            )
                        findNavController().navigate(action)
                    } else {
                        chatRoomId = UUID.randomUUID().toString()
                        Firebase.database.reference.child("Posts").child(postId)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val post = snapshot.getValue(PostItem::class.java)!!

                                    val newChatRoom = ChatItem(
                                        chatRoomId = chatRoomId,
                                        otherUserId = post.writerId,
                                        otherUserProfile = post.writerProfileImage,
                                        otherUserName = post.writerNickname,
                                    )
                                    chatRoomDB.setValue(newChatRoom)
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })

                        val action =
                            PostDetailFragmentDirections.actionPostDetailFragmentToChatDetailFragment(
                                chatRoomId = chatRoomId,
                                otherUserId = writerId
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }



        Firebase.database.reference.child("Posts").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) {
                        return
                    }
                    val postImagesList = mutableListOf<String>()
                    val postImagesSnapshot = snapshot.child("postImagesUrl")

                    for (postImageSnapshot in postImagesSnapshot.children) {
                        val postImageUrl = postImageSnapshot.getValue(String::class.java)
                        postImageUrl?.let { postImagesList.add(it) }
                    }

                    val loopingViewPager = binding.postImage
                    val indicator = binding.indicator

                    val postImageAdapter = PostImageAdapter(postImagesList, false)
                    loopingViewPager.adapter = postImageAdapter

                    indicator.highlighterViewDelegate = {
                        val highlighter = View(requireContext())
                        highlighter.layoutParams = FrameLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.indicator_width),
                            resources.getDimensionPixelSize(R.dimen.indicator_height)
                        )
                        highlighter.setBackgroundResource(R.drawable.indicator_circle)
                        highlighter
                    }
                    indicator.unselectedViewDelegate = {
                        val unselected = View(requireContext())
                        unselected.layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.indicator_width),
                            resources.getDimensionPixelSize(R.dimen.indicator_height)
                        )
                        unselected.setBackgroundResource(R.drawable.indicator_circle)
                        unselected.alpha = 0.4f
                        unselected
                    }

                    loopingViewPager.onIndicatorProgress = { selectingPosition, progress ->
                        indicator.onPageScrolled(
                            selectingPosition,
                            progress
                        )
                    }
                    indicator.updateIndicatorCounts(loopingViewPager.indicatorCount)

                    binding.writerName.text =
                        snapshot.child("writerNickname").getValue(String::class.java)
                    binding.writerStar.text =
                        snapshot.child("writerStar").getValue(Double::class.java).toString()

                    binding.postTitle.text =
                        snapshot.child("postTitle").getValue(String::class.java)

                    val priceWithoutCommas =
                        snapshot.child("postPrice").getValue(String::class.java)!!
                    val formattedPrice = StringBuilder()
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

                    binding.postPrice.text = "$formattedPrice 원"

                    binding.postContent.text =
                        snapshot.child("postContent").getValue(String::class.java)

                    binding.postStatus.apply {
                        if (snapshot.child("postStatus").getValue(Boolean::class.java) == true) {
                            text = "판매중"
                            backgroundTintList =
                                ContextCompat.getColorStateList(
                                    binding.root.context,
                                    R.color.main_color
                                )
                        } else {
                            text = "판매완료"
                            backgroundTintList =
                                ContextCompat.getColorStateList(binding.root.context, R.color.gray)
                            binding.safePaymentButton.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.light_gray)
                            binding.safePaymentButton.isClickable = false
                            binding.chatButton.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.light_gray)
                            binding.chatButton.isClickable = false

                            if(userId == writerId) {
                                binding.waybillButton.visibility = View.VISIBLE

                                binding.waybillButton.setOnClickListener {
                                    val action = PostDetailFragmentDirections.actionPostDetailFragmentToWaybillFragment(
                                        postId = postId,
                                        writerId = writerId
                                    )
                                    findNavController().navigate(action)
                                }
                            } else {
                                binding.waybillButton.visibility = View.GONE
                            }
                        }
                    }

                    if(snapshot.child("buyerId").exists()) {
                        if(userId == snapshot.child("buyerId").getValue(String::class.java)) {
                            binding.writeReview.visibility = View.VISIBLE

                            binding.writeReview.setOnClickListener {
                                val action =
                                    PostDetailFragmentDirections.actionPostDetailFragmentToReviewWriteFragment(
                                        profileUserId = writerId,
                                        postId = postId
                                    )
                                findNavController().navigate(action)
                            }
                        } else {
                            binding.writeReview.visibility = View.INVISIBLE
                        }
                    }

                    if(snapshot.child("reviewWrite").getValue(Boolean::class.java) == true) {
                        binding.writeReview.apply {
                            isClickable = false
                            val lightGrayColor = ContextCompat.getColorStateList(context, R.color.light_gray)
                            compoundDrawableTintList = lightGrayColor
                            setTextColor(lightGrayColor)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        Firebase.database.reference.child("Users").child(writerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child("userProfileImage").exists()) {
                        val userImageUri =
                            snapshot.child("userProfileImage").getValue(String::class.java)!!

                        Glide.with(binding.writerImage)
                            .load(userImageUri)
                            .into(binding.writerImage)
                    }

                    binding.writerStar.text =
                        snapshot.child("userStar").getValue(Double::class.java).toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.writerImage.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment(
                writerId = writerId,
                postId = postId
            )
            findNavController().navigate(action)
        }

        binding.writerName.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment(
                writerId = writerId,
                postId = postId
            )
            findNavController().navigate(action)
        }



        binding.editButton.setOnClickListener {
            var popupMenu = PopupMenu(context, it)

            popupMenu.menuInflater.inflate(R.menu.menu_post, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.post_edit_button -> {
                        val action =
                            PostDetailFragmentDirections.actionPostDetailFragmentToPostWriteFragment(
                                postId = postId
                            )
                        findNavController().navigate(action)
                    }

                    R.id.post_delete_button -> {
                        val builder = AlertDialog.Builder(context)
                        builder
                            .setTitle("게시글 삭제")
                            .setMessage("게시글을 삭제하시겠습니까?")
                            .setPositiveButton("삭제",
                                DialogInterface.OnClickListener { dialog, id ->

                                    Firebase.database.reference.child("Posts")
                                        .addValueEventListener(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                snapshot.children.forEach {
                                                    // 게시글 삭제
                                                    Firebase.database.reference.child("Posts")
                                                        .child(postId)
                                                        .removeValue()
                                                    // 사진 삭제
                                                    val imageRef =
                                                        Firebase.storage.reference.child(
                                                            "posts/${postId}"
                                                        )
                                                    imageRef.listAll()
                                                        .addOnSuccessListener { listResult ->
                                                            // Delete each image in the list
                                                            val deletePromises =
                                                                mutableListOf<Task<Void>>()
                                                            listResult.items.forEach { item ->
                                                                val deletePromise = item.delete()
                                                                deletePromises.add(deletePromise)
                                                            }

                                                            Tasks.whenAllComplete(deletePromises)
                                                                .addOnSuccessListener {
                                                                }
                                                                .addOnFailureListener { exception ->
                                                                }
                                                        }
                                                        .addOnFailureListener { exception ->
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })

                                    val action =
                                        PostDetailFragmentDirections.actionPostDetailFragmentToHomeFragment(
                                            "", false
                                        )
                                    findNavController().navigate(action)
                                })
                            .setNegativeButton("취소",
                                DialogInterface.OnClickListener { dialog, id -> })

                        builder.create()
                        builder.show()
                    }
                }
                false
            }
        }
    }
}