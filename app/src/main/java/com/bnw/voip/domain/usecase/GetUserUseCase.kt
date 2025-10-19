package com.bnw.voip.domain.usecase

import com.bnw.voip.data.model.User
import com.bnw.voip.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<User?> = profileRepository.getUser()
}