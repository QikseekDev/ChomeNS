/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtilities {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        RANDOM.nextBytes(iv);
        return iv;
    }

    private static SecretKeySpec createKeySpec(UUID secret) {
        return new SecretKeySpec(AESUtilities.getBytesFromUUID(secret), "AES");
    }

    public static byte[] encrypt(UUID secret, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] iv = AESUtilities.generateIV();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(1, (Key)AESUtilities.createKeySpec(secret), ivSpec);
        byte[] enc = cipher.doFinal(data);
        byte[] payload = new byte[iv.length + enc.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(enc, 0, payload, iv.length, enc.length);
        return payload;
    }

    public static byte[] decrypt(UUID secret, byte[] payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] iv = new byte[16];
        System.arraycopy(payload, 0, iv, 0, iv.length);
        byte[] data = new byte[payload.length - iv.length];
        System.arraycopy(payload, iv.length, data, 0, data.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(2, (Key)AESUtilities.createKeySpec(secret), ivSpec);
        return cipher.doFinal(data);
    }
}

