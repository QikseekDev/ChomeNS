/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import me.chayapak1.chomens_bot.Main;

public class DownloadUtilities {
    private static int limit = 0;

    public static byte[] DownloadToByteArray(URL url, int maxSize) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        if (limit > 3) {
            throw new IOException("NO !!!!!!");
        }
        ++limit;
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        try (BufferedInputStream downloadStream = new BufferedInputStream(conn.getInputStream());){
            int n;
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int tot = 0;
            while ((n = downloadStream.read(buf)) > 0) {
                byteArrayStream.write(buf, 0, n);
                if ((tot += n) > maxSize) {
                    throw new IOException("File is too large");
                }
                if (!Thread.interrupted()) continue;
                byte[] byArray = null;
                return byArray;
            }
            byte[] byArray = byteArrayStream.toByteArray();
            return byArray;
        }
    }

    static {
        Main.EXECUTOR.scheduleAtFixedRate(() -> {
            limit = 0;
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    public static class DefaultTrustManager
    implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

