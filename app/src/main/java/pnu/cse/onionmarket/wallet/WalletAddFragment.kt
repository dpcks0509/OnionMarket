package pnu.cse.onionmarket.wallet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentWalletAddBinding
import java.util.UUID

class WalletAddFragment : Fragment(R.layout.fragment_wallet_add) {
    private lateinit var binding: FragmentWalletAddBinding

    override fun onResume() {
        super.onResume()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(true)
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWalletAddBinding.bind(view)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.submitButton.setOnClickListener {

            if (binding.walletName.text.isNullOrEmpty() || binding.walletKey.text.isNullOrEmpty())
                Toast.makeText(context, "지갑 추가에 필요한 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()

            val walletId = UUID.randomUUID().toString()
            val userId = Firebase.auth.currentUser?.uid
            var walletImage = ""

            Firebase.database.reference.child("Users").child(userId!!).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child("userProfileImage").exists())
                        walletImage = snapshot.child("userProfileImage").getValue(String::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            val wallet = WalletItem(
                walletId = walletId,
                userId = userId,
                walletName = binding.walletName.text.toString(),
                walletImage = walletImage,
                privateKey = binding.walletKey.text.toString(),
                walletMoney = 0, // 지갑 연결 후 데이터 받아와서 수정하기
                createdAt = System.currentTimeMillis()
            )

            Firebase.database.reference.child("Wallets").child(walletId).setValue(wallet)
                .addOnSuccessListener {}

            findNavController().popBackStack()
        }
    }
}