package pnu.cse.onionmarket.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.chat.detail.ChatDetailFragment
import pnu.cse.onionmarket.databinding.FragmentChatBinding
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.post.detail.PostDetailFragmentDirections
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val userId = Firebase.auth.currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        chatAdapter = ChatAdapter { item ->
            val chatRoomDB = Firebase.database.reference.child("ChatRooms").child(userId!!)
                .child(item.otherUserId!!)

            val action =
                ChatFragmentDirections.actionChatFragmentToChatDetailFragment(
                    chatRoomId = item.chatRoomId!!,
                    otherUserId = item.otherUserId
                )
            findNavController().navigate(action)

            ChatDetailFragment.unreadMessage = 0

            val updates: MutableMap<String, Any> = hashMapOf(
                "ChatRooms/$userId/${item.otherUserId}/unreadMessage" to ChatDetailFragment.unreadMessage
            )

            Firebase.database.reference.updateChildren(updates)
        }

        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        val currentUserId = Firebase.auth.currentUser?.uid
        val chatRoomsDB = Firebase.database.reference.child("ChatRooms").child(currentUserId!!)

        chatRoomsDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = snapshot.children.map {
                    it.getValue(ChatItem::class.java)
                }
                chatAdapter.submitList(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
