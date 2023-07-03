package com.aivle.presentation.sharing.postDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.aivle.domain.model.sharingPost.Comment
import com.aivle.domain.model.sharingPost.SharingPostDetail
import com.aivle.presentation.R
import com.aivle.presentation.base.BaseActivity
import com.aivle.presentation.util.ext.repeatOnStarted
import com.aivle.presentation.databinding.ActivitySharingPostDetailBinding
import com.aivle.presentation.sharing.postDetail.SharingPostDetailViewModel.Event
import com.aivle.presentation.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "SharingPostDetailActivity"

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

        commentListAdapter = CommentListAdapter()
        binding.content.commentListView.adapter = commentListAdapter
    }

    private fun handleEvent() = repeatOnStarted {
        viewModel.eventFlow.collect { event -> when (event) {
            is Event.None -> {}
            is Event.LoadPost.Success -> {
                showPostDetail(event.postDetail)
            }
            is Event.LoadPost.Failure -> {
                showToast(event.message)
            }
        }}
    }

    private fun showPostDetail(postDetail: SharingPostDetail) {
        Log.d(TAG, "showPostDetail(): $postDetail")
        binding.postDetail = postDetail
        val comments = postDetail.comments.onEach {
            it.onClick = ::showReplyBottomSheet
        }
        commentListAdapter.submitList(comments)
    }

    private fun showReplyBottomSheet(comment: Comment) {
        val headerHeight = binding.header.btnBack.height
        Log.d(TAG, "showReplyBottomSheet(): headerHeight=$headerHeight")

        ReplyBottomSheetFragment.Builder(comment)
            .topOffset(headerHeight)
            .show(supportFragmentManager)
    }

    private fun loadSharingPostDetail() {
        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        viewModel.loadSharingPostDetail(postId)
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