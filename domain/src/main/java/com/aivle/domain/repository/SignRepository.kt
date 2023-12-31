package com.aivle.domain.repository

import com.aivle.domain.model.sign.SignInUser
import com.aivle.domain.model.sign.SignUpUser
import com.aivle.domain.response.NothingResponse
import com.aivle.domain.response.SignInWithTokenResponse
import com.aivle.domain.response.SignInResponse
import com.aivle.domain.response.SignUpResponse
import kotlinx.coroutines.flow.Flow

interface SignRepository {

    suspend fun signIn(signInUser: SignInUser): Flow<SignInResponse>

    suspend fun signInWithToken(): Flow<SignInWithTokenResponse>

    suspend fun signUp(signUpUser: SignUpUser): Flow<SignUpResponse>

    suspend fun signOut(): Flow<NothingResponse>

    suspend fun withdrawal(): Flow<NothingResponse>
}