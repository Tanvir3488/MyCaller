package com.bnw.voip.data.repository

import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val userManager: UserManager
) : ProfileRepository {
    override fun getUser(): Flow<User?> {
        return userManager.usernameFlow.map { username ->
            username?.let { User(it, "https://www.gravatar.com/avatar/") }
        }
    }
}