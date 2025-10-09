package com.bnw.voip.domain.usecase.call

import com.bnw.voip.data.repository.CallRepository
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(private val callRepository: CallRepository) {
    operator fun invoke(number: String) = callRepository.makeCall(number)
}
