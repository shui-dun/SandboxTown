package com.shuidun.sandbox_town_backend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class PasswordEncryptorTest {

    @Test
    void testEncryptedPasswdConsistency() {
        String password = "testPassword";
        String[] saltedHash1 = PasswordEncryptor.generateSaltedHash(password);
        String[] saltedHash2 = PasswordEncryptor.generateSaltedHash(password);

        assertNotEquals(saltedHash1[0], saltedHash2[0]); // Salts should be different
        assertNotEquals(saltedHash1[1], saltedHash2[1]); // Encrypted passwords should be different

        String encryptedPassword1 = PasswordEncryptor.encryptedPasswd(password, saltedHash1[0]);
        String encryptedPassword2 = PasswordEncryptor.encryptedPasswd(password, saltedHash2[0]);

        assertEquals(saltedHash1[1], encryptedPassword1); // Encrypted passwords should be the same
        assertEquals(saltedHash2[1], encryptedPassword2); // Encrypted passwords should be the same
    }
}
