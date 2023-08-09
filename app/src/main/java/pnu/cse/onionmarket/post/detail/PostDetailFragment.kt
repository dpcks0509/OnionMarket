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
import androidx.core.content.ContextCompat
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import pnu.cse.onionmarket.LoginActivity
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentPostDetailBinding
import pnu.cse.onionmarket.home.HomeFragmentDirections
import pnu.cse.onionmarket.post.PostItem

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
            binding.chatButton.text = "나의채팅"
            binding.chatButton.setOnClickListener {
                findNavController().navigate(R.id.action_postDetailFragment_to_chatFragment)
            }
        } else {
            binding.editButton.visibility = View.GONE
            binding.chatButton.text = "채팅하기"
            binding.chatButton.setOnClickListener {
                findNavController().navigate(R.id.action_postDetailFragment_to_chatDetailFragment)
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

                    val priceWithoutCommas = snapshot.child("postPrice").getValue(String::class.java)!!
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
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.writerInfo.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment(
                writerId = writerId
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