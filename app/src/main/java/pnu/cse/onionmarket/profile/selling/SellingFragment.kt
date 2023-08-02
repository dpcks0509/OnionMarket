package pnu.cse.onionmarket.profile.selling

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentSellingBinding
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.profile.ProfileFragmentDirections

class SellingFragment: Fragment(R.layout.fragment_selling) {
    private lateinit var binding: FragmentSellingBinding
    private lateinit var sellingAdapter: SellingAdapter

    private var sellingList = mutableListOf<PostItem>()
    private val userId = Firebase.auth.currentUser?.uid

    private var writerId: String? = null

    companion object {
        fun newInstance(writerId: String?): SellingFragment {
            val fragment = SellingFragment()
            val args = Bundle()
            args.putString("writerId", writerId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSellingBinding.bind(view)

        arguments?.let {
            // Retrieve the writerId from the arguments bundle
            writerId = it.getString("writerId")
        }

        sellingAdapter = SellingAdapter { post ->
            val action = ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(
                post.writerId.orEmpty(),
                post.postId.orEmpty()
            )
            findNavController().navigate(action)
        }

        if(writerId == null) {
            Firebase.database.reference.child("Posts")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sellingList = mutableListOf<PostItem>()

                        snapshot.children.forEach {
                            val post = it.getValue(PostItem::class.java)
                            post ?: return

                            if(post.writerId == userId)
                                sellingList.add(post)
                        }
                        sellingAdapter.sellingList = sellingList
                        sellingAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            Firebase.database.reference.child("Posts")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sellingList = mutableListOf<PostItem>()

                        snapshot.children.forEach {
                            val post = it.getValue(PostItem::class.java)
                            post ?: return

                            if(post.writerId == writerId)
                                sellingList.add(post)
                        }
                        sellingAdapter.sellingList = sellingList
                        sellingAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }



        binding.sellingRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sellingAdapter
        }


    }
}