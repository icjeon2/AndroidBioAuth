package kr.co.hi.hiup.util

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricFragment

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kr.co.hi.hiup.extension.evaluateJavascript
import org.jetbrains.anko.webView
import timber.log.Timber
import java.util.concurrent.Executor

object BioAuth {

    const val ACTION_BIOMETRIC_ENROLL = "Aandroid.settings.BIOMETRIC_ENROLL"
    const val EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED =
        "android.provider.extra.BIOMETRIC_AUTHENTICATORS_ALLOWED"

    private lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    lateinit var mListener: AuthListener

    fun canAuth(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        val responseCode = biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        when (responseCode) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("App can authenticate using biometrics.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.e("No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.e("Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                // Prompts the user to create credentials that your app accepts.
//                val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
//                    putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                }
//                startActivityForResult(enrollIntent, 1)
            }
        }
        return responseCode
    }

    fun initialize(context: Context, activity: Activity) {

        if (canAuth(context) != 0) {
            Timber.e("initialize failed!! code : ${canAuth(context)}")
            return
        }



        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(activity as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        context,
                        "인증 에러 $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    mListener.onAuthError()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        context,
                        "인증 성공", Toast.LENGTH_SHORT
                    )
                        .show()
                    mListener.onAuthSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        context, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    mListener.onAuthFailed()
                }
            })

        val authenticator =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
                BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            else BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증 로그인")
//            .setNegativeButtonText("Use account password")
//            .setDeviceCredentialAllowed(true)
            .setAllowedAuthenticators(authenticator)
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
//        val biometricLoginButton =
//            findViewById<Button>(R.id.biometric_login)
//        biometricLoginButton.setOnClickListener {
//            biometricPrompt.authenticate(promptInfo)
//        }
    }

    fun floatingAuth(mListener : AuthListener) {
        biometricPrompt.authenticate(promptInfo)
        this.mListener = mListener
    }

    interface AuthListener{
        fun onAuthError()
        fun onAuthSuccess()
        fun onAuthFailed()
    }

}
