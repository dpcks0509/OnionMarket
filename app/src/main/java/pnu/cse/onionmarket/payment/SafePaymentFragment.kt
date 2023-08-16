package pnu.cse.onionmarket.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentSafePaymentBinding


class SafePaymentFragment: Fragment(R.layout.fragment_safe_payment) {
    private lateinit var binding: FragmentSafePaymentBinding
    private val args: SafePaymentFragmentArgs by navArgs()

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
        binding = FragmentSafePaymentBinding.bind(view)
        val postId = args.postId

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            // 결제하고 판매자에게 알림보내기
            findNavController().popBackStack()
        }
    }
}