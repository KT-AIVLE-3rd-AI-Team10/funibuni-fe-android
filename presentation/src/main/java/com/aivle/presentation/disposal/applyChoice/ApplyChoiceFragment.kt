package com.aivle.presentation.disposal.applyChoice

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.aivle.presentation.R
import com.aivle.presentation.util.common.BitmapUtil
import com.aivle.presentation.databinding.FragmentApplyChoiceBinding
import com.aivle.presentation.disposal.base.BaseDisposalFragment
import java.text.DecimalFormat

class ApplyChoiceFragment : BaseDisposalFragment<FragmentApplyChoiceBinding>(R.layout.fragment_apply_choice) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
    }

    private fun initView() {
        val wasteSpec = activityViewModel.selectedWasteSpec ?: return
        val wasteImageBitmap = BitmapUtil.decodeFile(activityViewModel.wasteImageLocalUri)
        val categoryName = "${wasteSpec.large_category} (${wasteSpec.small_category})"
        val wasteFee = DecimalFormat("#,###").format(wasteSpec.fee) + "원"

        binding.wasteImage.setImageBitmap(wasteImageBitmap)
        binding.wasteName.text = categoryName
        binding.disposalFee.text = wasteFee
    }

    private fun initListener() {
        binding.wasteImageCardView.setOnClickListener {
            findNavController().navigate(R.id.action_applyChoiceFragment_to_wasteDisposalApplyFragment)
        }
        binding.sharingCardView.setOnClickListener {
            findNavController().navigate(R.id.action_applyChoiceFragment_to_createSharingPostFragment)
        }
    }
}