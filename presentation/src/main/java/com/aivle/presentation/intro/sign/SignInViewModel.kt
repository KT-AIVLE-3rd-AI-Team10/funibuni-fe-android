package com.aivle.presentation.intro.sign

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivle.domain.model.sign.SignInUser
import com.aivle.domain.response.SignInResponse
import com.aivle.domain.usecase.sign.SignInUseCase
import com.aivle.presentation.intro.firebase.FirebasePhoneAuthManager
import com.google.firebase.FirebaseException
import com.loggi.core_util.extensions.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val phoneAuthManager: FirebasePhoneAuthManager,
    private val SignInUseCase: SignInUseCase,
) : ViewModel() {

    private val _eventFlow: MutableStateFlow<Event> = MutableStateFlow(Event.None)
    val eventFlow: StateFlow<Event> get() = _eventFlow

    var authenticatingPhoneNumber: String = ""
        private set

    var isAuthCompleted: Boolean = false
        private set

    init {
        phoneAuthManager.setOnPhoneAuthCallback(OnMyPhoneAuthCallback())
    }

    fun initFirebase(activity: Activity) {
        phoneAuthManager.init(activity)
    }

    fun send(activity: Activity, phoneNumber: String) {
        viewModelScope.launch {
            _eventFlow.emit(Event.RequestSms.FirstTry.Loading)
        }
        phoneAuthManager.send(activity, phoneNumber.toFirebasePhoneFormat())
    }

    fun resend(activity: Activity, phoneNumber: String) {
        viewModelScope.launch {
            _eventFlow.emit(Event.RequestSms.Retry.Loading)
        }
        phoneAuthManager.resend(activity, phoneNumber.toFirebasePhoneFormat())
    }

    fun authenticateAndSignIn(activity: Activity, smsCode: String) {
        viewModelScope.launch {
            _eventFlow.emit(Event.RequestSms.AuthSmsCodeLoading)
        }
        phoneAuthManager.authenticate(activity, smsCode)
    }

    fun isSameAuthenticatingPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber == authenticatingPhoneNumber.replace("-", " ")
    }

    fun releaseFirebase() {
        phoneAuthManager.release()
    }

    private fun String.toFirebasePhoneFormat(): String {
        return "+82 " + this.replace(" ", "-")
    }

    private fun String.toFuniBuniPhoneFormat(): String {
        return this.drop(4)
    }

    sealed class Event {
        object None : Event()

        sealed class RequestSms : Event() {

            sealed class FirstTry : RequestSms() {
                object Loading : FirstTry()
                object Started : FirstTry()
            }
            sealed class Retry : RequestSms() {
                object Loading : Retry()
                object Started : Retry()
            }

            data class OnVerificationCompleted(val smsCode: String) : Event()
            data class OnVerificationFailed(val message: String?) : Event()

            object AuthSmsCodeLoading : RequestSms()
            object IncorrectCode : RequestSms()
            object Timeout : RequestSms()

            data class UpdateTimer(val remainingTime: String) : RequestSms()
            data class Exception(val message: String?) : RequestSms()
        }

        sealed class SignIn : Event() {
            object Succeed : SignIn()
            object NotExistsUser : SignIn()
            data class Exception(val message: String?) : SignIn()
        }

        data class ShowToast(val message: String?) : Event()
    }

    private inner class OnMyPhoneAuthCallback : FirebasePhoneAuthManager.OnPhoneAuthCallback {
        override fun onVerificationCompleted(smsCode: String) {
            log("onVerificationCompleted(): smsCode=$smsCode")

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.OnVerificationCompleted(smsCode))
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            log("onVerificationFailed(): e=$e")
            authenticatingPhoneNumber = ""

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.OnVerificationFailed(e.message))
            }
        }

        override fun onStarted(phoneNumber: String) {
            log("onStarted(): $phoneNumber")
            authenticatingPhoneNumber = phoneNumber.toFuniBuniPhoneFormat()

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.FirstTry.Started)
            }
        }

        override fun onReStarted(phoneNumber: String) {
            log("onReStarted(): $phoneNumber")
            authenticatingPhoneNumber = phoneNumber.toFuniBuniPhoneFormat()

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.Retry.Started)
            }
        }

        override fun onTimer(elapsedTimeSec: Int, remainingTimeString: String) {
            log("onTimer($elapsedTimeSec): $remainingTimeString")

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.UpdateTimer(remainingTimeString))
            }
        }

        override fun onTimeout() {
            log("onTimeout()")
            authenticatingPhoneNumber = ""

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.Timeout)
            }
        }

        override fun onPhoneAuthSucceed() {
            log("onPhoneAuthSucceed()")
            isAuthCompleted = true

            viewModelScope.launch {
                val signInUser = SignInUser(authenticatingPhoneNumber)
                SignInUseCase(signInUser)
                    .catch {
                        _eventFlow.emit(Event.ShowToast(it.message))
                    }
                    .collect { response -> when (response) {
                        is SignInResponse.Success -> _eventFlow.emit(Event.SignIn.Succeed)
                        is SignInResponse.Error.NotFoundUser -> _eventFlow.emit(Event.SignIn.NotExistsUser)
                        is SignInResponse.Exception -> _eventFlow.emit(
                            Event.SignIn.Exception(
                                response.message
                            )
                        )
                    }}
            }
        }

        override fun onPhoneAuthFailed() {
            log("onPhoneAuthFailed()")

            viewModelScope.launch {
                _eventFlow.emit(Event.RequestSms.IncorrectCode)
            }
        }
    }
}