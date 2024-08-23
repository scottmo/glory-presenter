package com.scottmo.core.security.api;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface CipherService {

    void encryptFile(String inFilePath, String outFilePath, String password)
            throws GeneralSecurityException, IOException;

    void decryptFile(String inFilePath, String outFilePath, String password)
            throws GeneralSecurityException, IOException;

    byte[] decrypt(String inFilePath, String password)
            throws GeneralSecurityException, IOException;

}
