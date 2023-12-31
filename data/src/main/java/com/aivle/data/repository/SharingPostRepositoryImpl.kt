package com.aivle.data.repository

import android.util.Log
import com.aivle.data.api.SharingPostApi
import com.aivle.data.di.api.FuniBuniApiQualifier
import com.aivle.data.mapper.toEntity
import com.aivle.data.mapper.toModel
import com.aivle.domain.model.sharingPost.Comment
import com.aivle.domain.model.sharingPost.Reply
import com.aivle.domain.model.sharingPost.SharingPostCreate
import com.aivle.domain.model.sharingPost.SharingPostDetail
import com.aivle.domain.model.sharingPost.SharingPostItem
import com.aivle.domain.repository.SharingPostRepository
import com.aivle.domain.response.DataResponse
import com.aivle.domain.response.NothingResponse
import com.loggi.core_util.extensions.log
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.ProtocolException
import javax.inject.Inject

class SharingPostRepositoryImpl @Inject constructor(
    @FuniBuniApiQualifier private val api: SharingPostApi
) : SharingPostRepository {

    override suspend fun createPost(post: SharingPostCreate): Flow<DataResponse<SharingPostItem>> = flow {
        log("createPost($post)")
        api.createPost(post.toEntity())
            .suspendOnSuccess {
                emit(DataResponse.Success(data.toModel()))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }

    override suspend fun getPosts(district: String): Flow<DataResponse<List<SharingPostItem>>> = flow {
        log("getPosts($district)")
        api.getPosts(district)
            .suspendOnSuccess {
                val posts = data.map { it.toModel() }
                    .sortedByDescending { it.created_at }
                emit(DataResponse.Success(posts))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }

    override suspend fun getPostDetail(post_id: Int): Flow<DataResponse<SharingPostDetail>> = flow {
        log("getPostDetail($post_id)")
        api.getPostDetail(post_id)
            .suspendOnSuccess {
                log("getPostDetail.suspendOnSuccess: $data")
                log("getPostDetail.suspendOnSuccess: ${data.toModel()}")
                emit(DataResponse.Success(data.toModel()))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }

    override suspend fun updatePost(post: SharingPostDetail): Flow<NothingResponse> = flow {
        log("updatePost($post)")
        api.updatePost(post.post_id, post.toEntity())
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun deletePost(post_id: Int): Flow<NothingResponse> = flow {
        log("deletePost($post_id)")
        try {
            val response = api.deletePost(post_id).execute()
            if (response.isSuccessful) {
                emit(NothingResponse.Success)
            } else {
                emit(NothingResponse.Failure.Error(response.errorBody()?.string()))
            }
        } catch (e: ProtocolException) {
            e.printStackTrace()
            emit(NothingResponse.Failure.Error(e.message))
        }
    }

    override suspend fun completeSharingPost(post_id: Int): Flow<NothingResponse> = flow {
        log("completeSharingPost($post_id)")
        api.completeSharingPost(post_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun reportPost(post_id: Int): Flow<NothingResponse> = flow {
        log("reportPost($post_id)")
        api.reportPost(post_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun likePost(post_id: Int): Flow<NothingResponse> = flow {
        log("likePost($post_id)")
        api.likePost(post_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun unlikePost(post_id: Int): Flow<NothingResponse> = flow {
        log("unlikePost($post_id)")
        api.unlikePost(post_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun addComment(post_id: Int, comment: String): Flow<DataResponse<Comment>> = flow {
        log("addComment($post_id): $comment")
        api.addComment(post_id, mapOf("comment" to comment))
            .suspendOnSuccess {
                emit(DataResponse.Success(data.toModel()))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }

    }

    override suspend fun updateComment(comment: Comment): Flow<NothingResponse> = flow {
        log("updateComment($comment)")
        api.updateComment(comment.post_id, comment.comment_id, mapOf("comment" to comment.comment))
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun deleteComment(
        post_id: Int,
        comment_id: Int
    ): Flow<NothingResponse> = flow {
        log("deleteComment($post_id, $comment_id)")
        api.deleteComment(post_id, comment_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun reportComment(
        post_id: Int,
        comment_id: Int
    ): Flow<NothingResponse> = flow {
        log("reportComment($post_id, $comment_id)")
        api.reportComment(post_id, comment_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun getReplies(
        post_id: Int,
        comment_id: Int
    ): Flow<DataResponse<List<Reply>>> = flow {
        api.getReplies(post_id, comment_id)
            .suspendOnSuccess { emit(DataResponse.Success(data.map { it.toModel() })) }
            .suspendOnFailure { emit(DataResponse.Failure.Error(this)) }
    }

    override suspend fun addReply(
        post_id: Int,
        comment_id: Int,
        reply: String
    ): Flow<DataResponse<Reply>> = flow {
        log("addReply($post_id, $comment_id): $reply")
        api.addReply(post_id, comment_id, mapOf("reply" to reply))
            .suspendOnSuccess { emit(DataResponse.Success(data.toModel())) }
            .suspendOnFailure { emit(DataResponse.Failure.Error(this)) }
    }

    override suspend fun updateReply(post_id: Int, reply: Reply): Flow<NothingResponse> = flow {
        log("updateReply($post_id): $reply")
        api.updateReply(post_id, reply.comment_id, reply.reply_id, mapOf("reply" to reply.reply))
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }

    override suspend fun deleteReply(
        post_id: Int,
        comment_id: Int,
        reply_id: Int
    ): Flow<NothingResponse> = flow {
        log("deleteReply($post_id, $comment_id, $reply_id)")
        try {
            val response = api.deleteReply(post_id, comment_id, reply_id).execute()
            if (response.isSuccessful) {
                emit(NothingResponse.Success)
            } else {
                emit(NothingResponse.Failure.Error(response.errorBody()?.string()))
            }
        } catch (e: ProtocolException) {
            e.printStackTrace()
            emit(NothingResponse.Success)
        }
    }

    override suspend fun reportReply(
        post_id: Int,
        comment_id: Int,
        reply_id: Int
    ): Flow<NothingResponse> = flow {
        log("reportReply($post_id, $comment_id, $reply_id)")
        api.reportReply(post_id, comment_id, reply_id)
            .suspendOnSuccess { emit(NothingResponse.Success) }
            .suspendOnFailure { emit(NothingResponse.Failure.Error(this)) }
    }
}