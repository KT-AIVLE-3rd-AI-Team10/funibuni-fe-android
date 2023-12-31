package com.aivle.domain.usecase.sharingPost

import com.aivle.domain.repository.SharingPostRepository
import javax.inject.Inject

class GetSharingPostListUserCase @Inject constructor(
    private val repository: SharingPostRepository
) {
    suspend operator fun invoke(district: String) = repository.getPosts(district)
}