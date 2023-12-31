package com.aivle.data.repository

import com.aivle.data.api.UserApi
import com.aivle.data.di.api.FuniBuniApiQualifier
import com.aivle.data.mapper.toModel
import com.aivle.domain.model.address.Address
import com.aivle.domain.model.user.User
import com.aivle.domain.repository.UserRepository
import com.aivle.domain.response.DataResponse
import com.aivle.domain.response.NothingResponse
import com.aivle.domain.usecase.user.UserInfo
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @FuniBuniApiQualifier private val api: UserApi
) : UserRepository {

    override suspend fun getUserInfo(): Flow<DataResponse<UserInfo>> = flow {
        api.getUserInfo()
            .suspendOnSuccess {
                emit(DataResponse.Success(data.toModel()))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }

    override suspend fun updateUserInfo(user: User): Flow<DataResponse<User>> = flow {
        val body = mapOf("nickname" to user.nickname!!)
        api.updateUserInfo(body)
            .suspendOnSuccess {
                emit(DataResponse.Success(data.toModel()))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }

    override suspend fun getAddresses(): Flow<DataResponse<List<Address>>> = flow {
        api.getAddresses()
            .suspendOnSuccess {
                emit(DataResponse.Success(data.map { it.toModel() }))
            }
            .suspendOnFailure {
                emit(DataResponse.Failure.Error(this))
            }
    }
}