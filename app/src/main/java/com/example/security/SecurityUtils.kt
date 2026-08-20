package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import java.security.MessageDigest

object SecurityUtils {

    /**
     * Hashes a PIN string securely using SHA-256 with a salt prefix.
     */
    fun hashPin(pin: String): String {
        if (pin.isBlank()) return ""
        val salted = "keshio_salt_v1_$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if input PIN matches the stored SHA-256 hash.
     */
    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || inputPin.isBlank()) return false
        return hashPin(inputPin) == storedHash
    }

    /**
     * Checks whether biometric authentication is supported and enrolled on this Android device.
     */
    fun canAuthenticateWithBiometrics(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }
}
