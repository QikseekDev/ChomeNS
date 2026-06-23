/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chomeNSMod;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static byte[] encrypt(byte[] data, String password) throws Exception {
        byte[] salt = Encryptor.generateRandomBytes(16);
        SecretKey key = Encryptor.deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = Encryptor.generateRandomBytes(16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(1, (Key)key, ivSpec);
        byte[] encrypted = cipher.doFinal(data);
        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);
        return combined;
    }

    public static byte[] decrypt(byte[] combined, String password) throws Exception {
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16 - 16];
        System.arraycopy(combined, 0, salt, 0, 16);
        System.arraycopy(combined, 16, iv, 0, 16);
        System.arraycopy(combined, 32, encrypted, 0, encrypted.length);
        SecretKey key = Encryptor.deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(2, (Key)key, ivSpec);
        return cipher.doFinal(encrypted);
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory2.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}

