package com.bnw.voip.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val PASSWORD_KEY = stringPreferencesKey("password")
    }

    suspend fun saveUserCredentials(username: String, password: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[PASSWORD_KEY] = password
        }
    }

    val usernameFlow: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }

    val passwordFlow: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[PASSWORD_KEY]
        }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
