package com.alerta.bitcoin.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HmacSha512Util {
    private static final String HMAC_SHA512_ALGORITHM = "HmacSHA512";

    public static String calculateHMAC(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA512_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac); // Converte para String hexadecimal
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erro ao calcular HMAC-SHA512", e);
        }
    }

    // Helper para converter byte array para String hexadecimal
    private static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b); // Formato de dois dígitos hexadecimais
        }
        return formatter.toString();
    }
}
