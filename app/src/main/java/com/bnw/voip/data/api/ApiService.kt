package com.bnw.voip.data.api

import com.bnw.voip.data.dto.request.UserBalanceRequestDto
import com.bnw.voip.data.dto.response.UserBalanceResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/getBalance")
    suspend fun getBalance(@Body userId: UserBalanceRequestDto): Flow<UserBalanceResponseDto>
}
