package pnu.cse.onionmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.databinding.ActivityJoinBinding
import java.util.*

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

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { join ->
                    val currentUser = Firebase.auth.currentUser
                    // 회원가입 성공
                    if(join.isSuccessful && currentUser != null) {
                        Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()

                        val userId = currentUser.uid

                        val user = mutableMapOf<String, Any>()
                        user["userId"] = userId
                        user["userPhone"] = phone
                        user["userNickname"] = nickname
                        user["userStar"] = 0.0

                        Firebase.database.reference.child("Users").child(userId).setValue(user)

                        Firebase.auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    // 회원가입 실패
                    else {
                        Toast.makeText(this,"회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


}