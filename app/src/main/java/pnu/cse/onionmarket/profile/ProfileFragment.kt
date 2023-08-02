package pnu.cse.onionmarket.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentProfileBinding
import pnu.cse.onionmarket.profile.review.ReviewFragment
import pnu.cse.onionmarket.profile.selling.SellingFragment

class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        Firebase.database.reference.child("Users").child(Firebase.auth.currentUser?.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.myName.text = snapshot.child("userNickname").getValue(String::class.java)
                    binding.myStar.text = snapshot.child("userStar").getValue(Double::class.java).toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        childFragmentManager.beginTransaction()
            .replace(R.id.myPageFrameLayout, SellingFragment())
            .commit()

        binding.sellingPost.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(R.id.myPageFrameLayout, SellingFragment())
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
}