package com.scottscmo.util

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

object Cryptor {
    private const val algorithm = "PBEWithMD5AndTripleDES"

    private fun cipher(password: String, salt: ByteArray, mode: Int): Cipher {
        val secretKey = SecretKeyFactory.getInstance(algorithm)
            .generateSecret(PBEKeySpec(password.toCharArray()))
        val pbeParameterSpec = PBEParameterSpec(salt, 100)

        return Cipher.getInstance(algorithm).apply {
            init(mode, secretKey, pbeParameterSpec)
        }
    }

    private fun runCipher(cipher: Cipher, inStream: InputStream): ByteArray {
        val outStream = ByteArrayOutputStream()

        val input = ByteArray(64)
        var read: Int
        while (inStream.read(input).also { read = it } != -1) {
            val output = cipher.update(input, 0, read)
            if (output != null) {
                outStream.write(output)
            }
        }

        val output = cipher.doFinal()
        if (output != null) {
            outStream.write(output)
        }

        return outStream.toByteArray()
    }

    fun encryptFile(inFilePath: String, outFilePath: String, password: String) {
        val salt = ByteArray(8).apply { Random().nextBytes(this) }
        val cipher = cipher(password, salt, Cipher.ENCRYPT_MODE)

        FileOutputStream(outFilePath).use { outFile ->
            FileInputStream(inFilePath).use { inFile ->
                val output = runCipher(cipher, inFile)
                outFile.write(salt)
                outFile.write(output)
            }
        }
    }

    fun decryptFile(inFilePath: String, outFilePath: String, password: String) {
        FileOutputStream(outFilePath).use { outFile ->
            val output = decrypt(inFilePath, password)
            outFile.write(output)
        }
    }

    fun decrypt(inFilePath: String, password: String): ByteArray {
        FileInputStream(inFilePath).use { inFile ->
            val salt = ByteArray(8).also { inFile.read(it) }
            val cipher = cipher(password, salt, Cipher.DECRYPT_MODE)

            return runCipher(cipher, inFile)
        }
    }
}
