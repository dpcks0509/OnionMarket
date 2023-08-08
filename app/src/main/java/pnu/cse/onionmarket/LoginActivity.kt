package pnu.cse.onionmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일 또는 패스워드가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { login ->
                    val currentUser = Firebase.auth.currentUser

                    // 로그인 성공
                    if(login.isSuccessful && currentUser != null) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    // 로그인 실패
                    else {
                        Toast.makeText(this, "이메일 또는 패스워드를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.gotoJoin.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }
}