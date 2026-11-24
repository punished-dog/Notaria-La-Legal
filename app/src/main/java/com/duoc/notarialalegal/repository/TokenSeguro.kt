package com.duoc.notarialalegal.repository

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Room guarda el token cifrado; la clave permanece en Android Keystore.
object TokenSeguro {
    private const val ALIAS = "notaria_token_xano"
    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existente = almacen.getKey(ALIAS, null)
        if (existente is SecretKey) return existente
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun cifrar(token: String): String {
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, clave())
        return Base64.encodeToString(cifrador.iv + cifrador.doFinal(token.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
    fun descifrar(valor: String): String {
        val bytes = Base64.decode(valor, Base64.NO_WRAP)
        require(bytes.size > 12)
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cifrador.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
    }
}
