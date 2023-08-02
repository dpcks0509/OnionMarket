package pnu.cse.onionmarket.home

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentHomeBinding
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.search.SearchAdapter
import pnu.cse.onionmarket.search.SearchItem
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePostAdapter: HomePostAdapter
    private lateinit var searchAdapter: SearchAdapter

    private var postList = mutableListOf<PostItem>()
    private var searchQuery: String? = null
    private var firstSearch: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        homePostAdapter = HomePostAdapter { post ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(
                    post.writerId.orEmpty(), post.postId.orEmpty()
                )
            )
        }

        searchAdapter = SearchAdapter {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        binding.homeRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = homePostAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        binding.searchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
                binding.searchview.clearFocus()

                filterList(query, true)
                val searchId = UUID.randomUUID().toString()
                searchAdapter.addItem(SearchItem(searchId, query))
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    filterList(newText, false)
                }
                return false
            }
        })

        binding.postWriteButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_postWriteFragment)
        }

        Firebase.database.reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList = mutableListOf<PostItem>()

                    snapshot.children.forEach {
                        val post = it.getValue(PostItem::class.java)
                        post ?: return
                        postList.add(post)
                    }

                    searchQuery = arguments?.getString("searchQuery")
                    if (firstSearch == null)
                        firstSearch = arguments?.getBoolean("firstSearch")
                    if (!searchQuery.isNullOrEmpty() && firstSearch!!) {
                        binding.searchview.setQuery(searchQuery, false)
                        filterList(searchQuery, true)
                        firstSearch = false
                    } else {
                        binding.searchview.setQuery("", false)
                        filterList("", false)
                    }

                    homePostAdapter.setOriginalList(postList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filterList(query: String?, onSearching: Boolean) {
        val filteredList: MutableList<PostItem> = mutableListOf()
        for (post in postList) {
            if (post.postTitle?.contains(query?.trim() ?: "", ignoreCase = true) == true) {
                filteredList.add(post)
            }
        }
        homePostAdapter.setFilteredList(filteredList, onSearching)
    }
}