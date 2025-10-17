package com.bnw.voip.domain.usecase.call

import com.bnw.voip.data.repository.CallRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val callRepository: CallRepository) {
    operator fun invoke(username: String, password: String) = callRepository.login(username, password)
}
