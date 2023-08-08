package pnu.cse.onionmarket.chat.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

        chatDetailAdapter = ChatDetailAdapter(binding.nickname)

        chatDetailAdapter.chatDetailList = chatDetailList

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatDetailAdapter
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        // 데이터 예시
        with(chatDetailList) {
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
            add(ChatDetailItem("1", "1", R.drawable.app_logo, "번데기껍질", "안녕하세요", "10:15 오후"))
        }
    }
}