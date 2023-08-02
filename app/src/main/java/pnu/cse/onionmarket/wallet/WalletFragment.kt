package pnu.cse.onionmarket.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentWalletBinding


class WalletFragment: Fragment(R.layout.fragment_wallet) {
    private lateinit var binding: FragmentWalletBinding
    private lateinit var walletAdapter: WalletAdapter

    private val walletList = mutableListOf<WalletItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWalletBinding.bind(view)

        walletAdapter = WalletAdapter()
        walletAdapter.walletList = walletList

        binding.walletRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = walletAdapter
        }

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_walletFragment_to_walletAddFragment)
        }


        // 데이터 예시
        with(walletList) {
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
            add(WalletItem("1", "번데기껍질님의 지갑", 1000000))
        }
    }
}