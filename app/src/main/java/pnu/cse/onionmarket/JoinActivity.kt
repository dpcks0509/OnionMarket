package pnu.cse.onionmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.databinding.ActivityJoinBinding
import java.util.*
import java.util.concurrent.TimeUnit

class JoinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinButton.setOnClickListener {
            val email = binding.joinEmail.text.toString()
            val password = binding.joinPassword.text.toString()
            val phone = binding.joinPhone.text.toString()
            val nickname = binding.joinNickname.text.toString()

            if(email.isEmpty() || password.isEmpty() || phone.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "회원가입에 필요한 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.database.reference.child("Users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var nicknameExists = false
                        var phoneExits = false

                        for (userSnapshot in snapshot.children) {
                            val userNickname = userSnapshot.child("userNickname").getValue(String::class.java)
                            if (userNickname == nickname) {
                                nicknameExists = true
                                break
                            }
                        }

                        for (userSnapshot in snapshot.children) {
                            val userPhone = userSnapshot.child("userPhone").getValue(String::class.java)
                            if (userPhone == phone) {
                                phoneExits = true
                                break
                            }
                        }

                        if (nicknameExists) {
                            Toast.makeText(applicationContext, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show()
                        } else if(phoneExits) {
                            Toast.makeText(applicationContext, "이미 가입된 핸드폰번호입니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Firebase.auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { join ->
                                    val currentUser = Firebase.auth.currentUser
                                    // 회원가입 성공
                                    if(join.isSuccessful && currentUser != null) {
                                        Toast.makeText(this@JoinActivity, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()

                                        val userId = currentUser.uid

                                        val user = mutableMapOf<String, Any>()
                                        user["userId"] = userId
                                        user["userPhone"] = phone
                                        user["userNickname"] = nickname
                                        user["userStar"] = 0.0

                                        Firebase.database.reference.child("Users").child(userId).setValue(user)

                                        Firebase.auth.signOut()
                                        startActivity(Intent(this@JoinActivity, LoginActivity::class.java))
                                    }
                                    // 회원가입 실패
                                    else {
                                        Toast.makeText(this@JoinActivity,"회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })


        }
    }
}