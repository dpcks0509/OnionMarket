package pnu.cse.onionmarket.chat.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentChatDetailBinding

class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {
    private lateinit var binding: FragmentChatDetailBinding
    private lateinit var chatDetailAdapter: ChatDetailAdapter

    private val chatDetailList = mutableListOf<ChatDetailItem>()

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
        binding = FragmentChatDetailBinding.bind(view)

        val mainActivity = activity as MainActivity

        chatDetailAdapter = ChatDetailAdapter()

        chatDetailAdapter.chatDetailList = chatDetailList

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatDetailAdapter
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.postInfo.setOnClickListener {
            // 자신의 게시글인지 아닌지 확인, postId도 확인
            val writerId = Firebase.auth.currentUser?.uid
            findNavController().navigate(R.id.action_chatDetailFragment_to_postDetailFragment)
        }

        // 정보 받아오기 (수정)
        Glide.with(binding.postThumbnail)
            .load(R.drawable.baseline_image_24)
            .into(binding.postThumbnail)

        binding.postTitle.text = "먹태깡"
        binding.postPrice.text = "3000원"

        // 데이터 예시
        with(chatDetailList) {
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
            add(ChatDetailItem("1", "1", R.drawable.baseline_person_24,"번데기껍질", "안녕하세요"))
        }
    }

}