package pnu.cse.onionmarket.profile

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentProfileBinding
import pnu.cse.onionmarket.home.HomeFragmentArgs
import pnu.cse.onionmarket.profile.review.ReviewFragment
import pnu.cse.onionmarket.profile.selling.SellingFragment

class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        val userId = Firebase.auth.currentUser?.uid
        val writerId = arguments?.getString("writerId")

        binding.settingButton.visibility = View.VISIBLE
        binding.profileText.setPadding(dpToPx(48),0,0,0)
        Firebase.database.reference.child("Users").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.userNickname.text = snapshot.child("userNickname").getValue(String::class.java)
                    binding.userStar.text = snapshot.child("userStar").getValue(Double::class.java).toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        if(userId == writerId) {
            binding.settingButton.visibility = View.VISIBLE
            binding.profileText.setPadding(dpToPx(48),0,0,0)
            Firebase.database.reference.child("Users").child(userId!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.userNickname.text = snapshot.child("userNickname").getValue(String::class.java)
                        binding.userStar.text = snapshot.child("userStar").getValue(Double::class.java).toString()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            if(writerId != null) {
                binding.settingButton.visibility = View.GONE
                binding.profileText.setPadding(0,0,0,0)
                Firebase.database.reference.child("Users").child(writerId!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            binding.userNickname.text = snapshot.child("userNickname").getValue(String::class.java)
                            binding.userStar.text = snapshot.child("userStar").getValue(Double::class.java).toString()
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }


        val sellingFragment = SellingFragment.newInstance(writerId)
        childFragmentManager.beginTransaction()
            .replace(R.id.myPageFrameLayout, sellingFragment)
            .commit()

        binding.sellingPost.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.myPageFrameLayout, sellingFragment)
                .commit()
        }

        binding.review.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.myPageFrameLayout, ReviewFragment())
                .commit()
        }


        binding.settingButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
        }
    }

    fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

}