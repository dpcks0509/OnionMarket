package pnu.cse.onionmarket.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import pnu.cse.onionmarket.LoginActivity
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentProfileBinding
import pnu.cse.onionmarket.home.HomeFragmentArgs
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.profile.review.ReviewFragment
import pnu.cse.onionmarket.profile.selling.SellingFragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding

    private var googleSignInClient: GoogleSignInClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        val userId = Firebase.auth.currentUser?.uid
        val writerId = arguments?.getString("writerId")

        binding.settingButton.setOnClickListener {
            var popupMenu = PopupMenu(context, it)

            popupMenu.menuInflater.inflate(R.menu.menu_setting, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logout_button -> {
                        FirebaseAuth.getInstance().signOut()
                        googleSignInClient?.signOut()

                        var signOutIntent = Intent(context, LoginActivity::class.java)
                        signOutIntent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(signOutIntent)
                    }
                    R.id.withdrawal_button -> {
                        val builder = AlertDialog.Builder(context)
                        builder
                            .setTitle("회원탈퇴")
                            .setMessage("정말로 회원탈퇴를 하시겠습니까?\n회원탈퇴시 이후 복구가 불가능합니다.")
                            .setPositiveButton("탈퇴",
                                DialogInterface.OnClickListener { dialog, id ->
                                    // 회원탈퇴
                                    val user = Firebase.auth.currentUser
                                    user?.delete()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val postList: MutableList<PostItem> =
                                                    mutableListOf()
                                                Firebase.database.reference.child("Posts")
                                                    .addValueEventListener(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {

                                                            snapshot.children.forEach {
                                                                val post =
                                                                    it.getValue(PostItem::class.java)
                                                                post ?: return

                                                                if (post.writerId == userId) {
                                                                    postList.add(post)

                                                                    // 게시글 삭제
                                                                    Firebase.database.reference.child(
                                                                        "Posts"
                                                                    )
                                                                        .child(post.postId!!)
                                                                        .removeValue()
                                                                    // 사진 삭제
                                                                    val imageRef =
                                                                        Firebase.storage.reference.child(
                                                                            "posts/${post.postId}"
                                                                        )
                                                                    imageRef.listAll()
                                                                        .addOnSuccessListener { listResult ->
                                                                            // Delete each image in the list
                                                                            val deletePromises =
                                                                                mutableListOf<Task<Void>>()
                                                                            listResult.items.forEach { item ->
                                                                                val deletePromise =
                                                                                    item.delete()
                                                                                deletePromises.add(
                                                                                    deletePromise
                                                                                )
                                                                            }

                                                                            Tasks.whenAllComplete(
                                                                                deletePromises
                                                                            )
                                                                                .addOnSuccessListener {
                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                }
                                                                        }
                                                                        .addOnFailureListener { exception ->
                                                                        }
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })

                                                Firebase.database.reference.child("Users")
                                                    .child(userId!!).removeValue()

                                                FirebaseAuth.getInstance().signOut()
                                                googleSignInClient?.signOut()

                                                val signOutIntent =
                                                    Intent(context, LoginActivity::class.java)
                                                signOutIntent.flags =
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(signOutIntent)


                                            }
                                        }


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

        binding.settingButton.visibility = View.VISIBLE
        Firebase.database.reference.child("Users").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.userNickname.text =
                        snapshot.child("userNickname").getValue(String::class.java)
                    binding.userStar.text =
                        snapshot.child("userStar").getValue(Double::class.java).toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        if (userId == writerId) {
            binding.settingButton.visibility = View.VISIBLE

            Firebase.database.reference.child("Users").child(userId!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.userNickname.text =
                            snapshot.child("userNickname").getValue(String::class.java)
                        binding.userStar.text =
                            snapshot.child("userStar").getValue(Double::class.java).toString()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            if (writerId != null) {
                binding.settingButton.visibility = View.INVISIBLE
                Firebase.database.reference.child("Users").child(writerId!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            binding.userNickname.text =
                                snapshot.child("userNickname").getValue(String::class.java)
                            binding.userStar.text =
                                snapshot.child("userStar").getValue(Double::class.java).toString()
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


    }
}