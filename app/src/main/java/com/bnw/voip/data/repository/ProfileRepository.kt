package com.bnw.voip.data.repository

import com.bnw.voip.data.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUser(): Flow<User?>
}