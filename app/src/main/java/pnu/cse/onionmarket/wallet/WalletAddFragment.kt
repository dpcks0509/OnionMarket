package pnu.cse.onionmarket.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentWalletAddBinding



class WalletAddFragment: Fragment(R.layout.fragment_wallet_add) {
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
    }
}