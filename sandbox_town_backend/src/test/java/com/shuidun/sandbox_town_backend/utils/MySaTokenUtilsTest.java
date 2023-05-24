package com.shuidun.sandbox_town_backend.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class MySaTokenUtilsTest {

    @Test
    void testGenerateSaltedHash() {
        String password = "testPassword";
        String[] saltedHash = MySaTokenUtils.generateSaltedHash(password);
        assertNotNull(saltedHash);
        assertEquals(2, saltedHash.length);
        assertNotNull(saltedHash[0]);
        assertNotNull(saltedHash[1]);
    }

    @Test
    void testEncryptedPasswd() {
        String password = "testPassword";
        String salt = "testSalt";
        String encryptedPassword = MySaTokenUtils.encryptedPasswd(password, salt);
        assertNotNull(encryptedPassword);
        assertNotEquals(password, encryptedPassword);
    }

    @Test
    void testEncryptedPasswdConsistency() {
        String password = "testPassword";
        String[] saltedHash1 = MySaTokenUtils.generateSaltedHash(password);
        String[] saltedHash2 = MySaTokenUtils.generateSaltedHash(password);

        assertNotEquals(saltedHash1[0], saltedHash2[0]); // Salts should be different
        assertNotEquals(saltedHash1[1], saltedHash2[1]); // Encrypted passwords should be different

        String encryptedPassword1 = MySaTokenUtils.encryptedPasswd(password, saltedHash1[0]);
        String encryptedPassword2 = MySaTokenUtils.encryptedPasswd(password, saltedHash2[0]);

        assertEquals(saltedHash1[1], encryptedPassword1); // Encrypted passwords should be the same
        assertEquals(saltedHash2[1], encryptedPassword2); // Encrypted passwords should be the same
    }
}
