package pnu.cse.onionmarket.post.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentPostDetailBinding

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

        Firebase.database.reference.child("Posts").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!isAdded) {
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

                    val postImageAdapter = PostImageAdapter(postImagesList,false)
                    loopingViewPager.adapter = postImageAdapter



                    indicator.highlighterViewDelegate = {
                        val highlighter = View(requireContext())
                        highlighter.layoutParams = FrameLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.indicator_width),
                            resources.getDimensionPixelSize(R.dimen.indicator_height))
                        highlighter.setBackgroundResource(R.drawable.indicator_circle)
                        highlighter
                    }
                    indicator.unselectedViewDelegate = {
                        val unselected = View(requireContext())
                        unselected.layoutParams = LinearLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.indicator_width),
                            resources.getDimensionPixelSize(R.dimen.indicator_height))
                        unselected.setBackgroundResource(R.drawable.indicator_circle)
                        unselected.alpha = 0.4f
                        unselected
                    }

                    loopingViewPager.onIndicatorProgress = { selectingPosition, progress -> indicator.onPageScrolled(selectingPosition, progress) }
                    indicator.updateIndicatorCounts(loopingViewPager.indicatorCount)

                    binding.writerName.text = snapshot.child("writerNickname").getValue(String::class.java)
                    binding.writerStar.text = snapshot.child("writerStar").getValue(Double::class.java).toString()

                    binding.postTitle.text = snapshot.child("postTitle").getValue(String::class.java)
                    binding.postPrice.text = snapshot.child("postPrice").getValue(String::class.java)
                    binding.postContent.text = snapshot.child("postContent").getValue(String::class.java)
                    binding.postStatus.apply {
                        if (snapshot.child("postStatus").getValue(Boolean::class.java) == true) {
                            text = "판매중"
                            backgroundTintList =
                                ContextCompat.getColorStateList(binding.root.context, R.color.main_color)
                        } else {
                            text = "판매완료"
                            backgroundTintList =
                                ContextCompat.getColorStateList(binding.root.context, R.color.gray)
                        }
                    }

                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.writerInfo.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment(
                writerId = writerId
            )
            findNavController().navigate(action)
        }

        binding.chatButton.setOnClickListener {
            findNavController().navigate(R.id.action_postDetailFragment_to_chatDetailFragment)
        }
    }
}