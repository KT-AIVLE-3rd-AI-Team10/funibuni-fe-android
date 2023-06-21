package com.aivle.presentation.sharingPostDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.aivle.presentation.R
import com.aivle.presentation.base.BaseActivity
import com.aivle.presentation.common.repeatOnStarted
import com.aivle.presentation.databinding.ActivitySharingPostDetailBinding
import com.aivle.presentation.sharing.CommentListAdapter
import com.aivle.presentation.sharingPostDetail.SharingPostDetailViewModel.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingPostDetailActivity : BaseActivity<ActivitySharingPostDetailBinding>(R.layout.activity_sharing_post_detail) {

    private val viewModel: SharingPostDetailViewModel by viewModels()

    private lateinit var headerAdapter: SharingPostDetailHeaderAdapter
    private lateinit var commentListAdapter: CommentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        handleEvent()
        loadSharingPostDetail()
    }

    private fun initView() {
        headerAdapter = SharingPostDetailHeaderAdapter(binding.header)
            .init(window, binding.appBar)
            .onBackPressed { finish() }

        binding.content.commentListView.adapter = CommentListAdapter().also {
            commentListAdapter = it
        }
    }

    private fun handleEvent() = repeatOnStarted {
        viewModel.eventFlow.collect { event -> when (event) {
            is Event.None -> {}
            is Event.LoadPost.Success -> {
                val postDetail = event.postDetail
                binding.postDetail = postDetail
                commentListAdapter.submitList(postDetail.comments)
            }
            is Event.LoadPost.Failure -> {
                showToast(event.message)
            }
        }}
    }

    private fun loadSharingPostDetail() {
        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        viewModel.loadSharingPostDetail(postId)
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {

        private const val EXTRA_POST_ID = "post_id"

        fun getIntent(context: Context, postId: Int): Intent {
            return Intent(context, SharingPostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
            }
        }
    }
}