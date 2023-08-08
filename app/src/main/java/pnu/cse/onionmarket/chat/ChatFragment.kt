package pnu.cse.onionmarket.chat

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentChatBinding

class ChatFragment: Fragment(R.layout.fragment_chat) {
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val chatList = mutableListOf<ChatItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        chatAdapter = ChatAdapter { item ->
            findNavController().navigate(R.id.action_chatFragment_to_chatDetailFragment)
        }

        chatAdapter.chatList = chatList

        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        // 데이터 예시
        with(chatList) {
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
            chatList.add(ChatItem("1","1",R.drawable.app_logo,"번데기껍질","안녕하세요","3시간전",1))
        }
    }
}