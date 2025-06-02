package com.alerta.bitcoin.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MercadoBitcoinApiUtil {
    // Método para gerar o MAC (Message Authentication Code) para a assinatura
    public static String generateSignature(String secretKey, String message) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key_spec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secret_key_spec);
            byte[] mac_data = sha512_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(mac_data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Logar o erro ou lançar uma exceção mais específica
            throw new RuntimeException("Erro ao gerar assinatura HMAC-SHA512", e);
        }
    }

    // Método para obter o timestamp Unix atual em milissegundos (para tapi_id)
    public static long getUnixTimestamp() {
        return System.currentTimeMillis() / 1000; // Divide por 1000 para segundos
    }
}
