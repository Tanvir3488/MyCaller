package com.bnw.voip.ui.main.dialer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bnw.voip.domain.usecase.call.MakeCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DialerViewModel @Inject constructor(
    private val makeCallUseCase: MakeCallUseCase
) : ViewModel() {

    private val _phoneNumber = MutableLiveData<String>("")
    val phoneNumber: LiveData<String> = _phoneNumber

    fun onDigitPressed(digit: String) {
        _phoneNumber.value += digit
    }

    fun onDeletePressed() {
        _phoneNumber.value = _phoneNumber.value?.dropLast(1)
    }

    fun onCallPressed() {
        phoneNumber.value?.let {
            if (it.isNotEmpty()) {
                makeCallUseCase(it)
            }
        }
    }
}