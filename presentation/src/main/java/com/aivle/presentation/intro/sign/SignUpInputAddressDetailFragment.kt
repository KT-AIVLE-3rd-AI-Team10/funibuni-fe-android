package com.aivle.presentation.intro.sign

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.Guideline
import androidx.core.widget.addTextChangedListener
import com.aivle.presentation.R
import com.aivle.presentation.util.ext.repeatOnStarted
import com.aivle.presentation.databinding.FragmentSignUpInputAddressDetailBinding
import com.loggi.core_util.extensions.log
import kotlinx.coroutines.flow.collectLatest

class SignUpInputAddressDetailFragment
    : BaseSignFragment<FragmentSignUpInputAddressDetailBinding>(R.layout.fragment_sign_up_input_address_detail) {

    override val bottomButtonGuideLine: Guideline
        get() = binding.bottomButtonGuideline

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        handleEvent()
    }

    private fun initView() {
        binding.edtAddressDetail.addTextChangedListener {
            binding.btnSignUp.isEnabled = (it?.isNotBlank() == true)
        }

        binding.btnSignUp.setOnClickListener {
            val addressDetail = binding.edtAddressDetail.text.toString()
            signViewModel.signUp(addressDetail)
        }
    }

    private fun handleEvent() = repeatOnStarted {
        signViewModel.addressEventFlow.collectLatest { address ->
            log("addressEventFlow.collectLatest: $address")
            val addressName = address.road_address!!.address_name
            val roadAddressName = address.road_address!!.address_name

            binding.addressName = addressName
            binding.roadAddressName = roadAddressName

            if (roadAddressName == "서울 송파구 올림픽로 326") {
                binding.edtAddressDetail.setText("송파구청")
            }
        }
    }
}