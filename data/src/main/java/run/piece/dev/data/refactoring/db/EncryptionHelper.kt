package run.piece.dev.data.refactoring.db

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class EncryptionHelper {
    private val KEY_ALIAS = "QHRva3Rva2hhbi5kZXYvcGllY2U="

    init {
        // 키 저장소 생성 및 초기화
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // 키가 없으면 생성
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )

            keyGenerator.init(KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false) // API 레벨 28부터 true 가능..
                .build()
            )

            keyGenerator.generateKey()
        }
    }

    // 데이터 암호화
    fun encryptData(dataToEncrypt: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return cipher.doFinal(dataToEncrypt)
    }

    // 데이터 복호화
    fun decryptData(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        return cipher.doFinal(encryptedData)
    }
}
