package pnu.cse.onionmarket

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.chat.ChatAdapter
import pnu.cse.onionmarket.chat.detail.ChatDetailFragment.Companion.unreadMessage
import pnu.cse.onionmarket.databinding.ActivityMainBinding
import pnu.cse.onionmarket.home.HomeFragmentDirections

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onResume() {
        super.onResume()

        // 알림 제거
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(0)
    }

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

        // notification
        val myUserId = Firebase.auth.currentUser?.uid
        val chatRoomId = intent.getStringExtra("chatRoomId")
        val otherUserId = intent.getStringExtra("otherUserId")
        if(!chatRoomId.isNullOrEmpty()) {
            unreadMessage = 0

            val updates: MutableMap<String, Any> = hashMapOf(
                "ChatRooms/$myUserId/$otherUserId/unreadMessage" to unreadMessage
            )

            Firebase.database.reference.updateChildren(updates)

            val action = HomeFragmentDirections.actionHomeFragmentToChatDetailFragment(
                chatRoomId = chatRoomId,
                otherUserId = otherUserId!!
            )
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(action)
        }

        // navigation 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNavigationview.setupWithNavController(navHostFragment.navController)

        askNotificationPermission()

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {

        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationalDialog()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("채팅 알림을 받기위해서 알림 권한이 필요합니다.")
            .setPositiveButton("알림 허용") { _,_ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    fun hideBottomNavigation(hide: Boolean) {
        if(hide)
            binding.bottomNavigationview.visibility = View.GONE
        else
            binding.bottomNavigationview.visibility = View.VISIBLE
    }
}