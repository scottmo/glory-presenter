package com.scottmo.core.security.impl;

import com.scottmo.core.security.api.CipherService;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Random;

public final class CipherServiceImpl implements CipherService {
    private static final String algorithm = "PBEWithMD5AndTripleDES";

    private Cipher cipher(String password, byte[] salt, int mode) throws GeneralSecurityException {
        SecretKey secretKey = SecretKeyFactory.getInstance(algorithm)
            .generateSecret(new PBEKeySpec(password.toCharArray()));
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(mode, secretKey, pbeParameterSpec);

        return cipher;
    }

    private byte[] runCipher(Cipher cipher, InputStream inStream) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        byte[] input = new byte[64];
        int read;
        while ((read = inStream.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, read);
            if (output != null) {
                outStream.write(output);
            }
        }

        byte[] output = cipher.doFinal();
        if (output != null) {
            outStream.write(output);
        }

        return outStream.toByteArray();
    }

    @Override
    public void encryptFile(String inFilePath, String outFilePath, String password)
            throws GeneralSecurityException, IOException {
        byte[] salt = new byte[8];
        new Random().nextBytes(salt);

        Cipher cipher = cipher(password, salt, Cipher.ENCRYPT_MODE);

        byte[] output;
        try (FileInputStream inFile = new FileInputStream(inFilePath)) {
            output = runCipher(cipher, inFile);
        }
        try (FileOutputStream outFile = new FileOutputStream(outFilePath)) {
            outFile.write(salt);
            outFile.write(output);
        }
    }

    @Override
    public void decryptFile(String inFilePath, String outFilePath, String password)
            throws GeneralSecurityException, IOException {
        byte[] output = decrypt(inFilePath, password);
        try (FileOutputStream outFile = new FileOutputStream(outFilePath)) {
            outFile.write(output);
        }
    }

    @Override
    public byte[] decrypt(String inFilePath, String password)
            throws GeneralSecurityException, IOException {
        try (FileInputStream inFile = new FileInputStream(inFilePath)) {
            byte[] salt = new byte[8];
            inFile.read(salt);
            Cipher cipher = cipher(password, salt, Cipher.DECRYPT_MODE);
            return runCipher(cipher, inFile);
        }
    }
}
