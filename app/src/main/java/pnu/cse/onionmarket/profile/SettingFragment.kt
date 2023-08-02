package pnu.cse.onionmarket.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import pnu.cse.onionmarket.LoginActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentSettingBinding

class SettingFragment: Fragment(R.layout.fragment_setting) {
    private lateinit var binding: FragmentSettingBinding

    private var googleSignInClient: GoogleSignInClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingBinding.bind(view)

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()

            var signOutIntent = Intent(context, LoginActivity::class.java)
            signOutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(signOutIntent)
        }

    }
}