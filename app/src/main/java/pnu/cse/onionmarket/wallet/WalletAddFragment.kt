package pnu.cse.onionmarket.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentWalletAddBinding



class WalletAddFragment: Fragment(R.layout.fragment_wallet_add) {
    private lateinit var binding: FragmentWalletAddBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWalletAddBinding.bind(view)


    }
}