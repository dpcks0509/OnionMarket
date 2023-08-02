package pnu.cse.onionmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 상태가 아니면 로그인 창으로 이동
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // navigation 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNavigationview.setupWithNavController(navHostFragment.navController)

    }

    fun hideBottomNavigation(hide: Boolean) {
        if(hide)
            binding.bottomNavigationview.visibility = View.GONE
        else
            binding.bottomNavigationview.visibility = View.VISIBLE
    }
}