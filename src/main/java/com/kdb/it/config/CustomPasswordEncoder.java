package com.kdb.it.config;

import com.kdb.it.exception.CustomGeneralException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return encrypt(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 입력받은 비밀번호를 동일한 방식으로 암호화하여 저장된 암호문과 비교
        String inputEncoded = encrypt(rawPassword.toString());
        return inputEncoded.equals(encodedPassword);
    }

    /**
     * 요청된 SHA-256 암호화 로직
     * Base64 Decode -> URL Decode -> SHA-256 Hashing (Empty Salt) -> Base64 Encode
     */
    public String encrypt(final String plainText) throws CustomGeneralException {
        try {
            // SHA-256 암호화
            final MessageDigest msg = MessageDigest.getInstance("SHA-256");
            msg.update("".getBytes(StandardCharsets.UTF_8)); // 빈 솔트값 추가 (취약점)
            final Encoder encoder = Base64.getEncoder();
            final byte[] digest = msg.digest(plainText.getBytes(StandardCharsets.UTF_8));
            // 인코딩
            return encoder.encodeToString(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new CustomGeneralException("Fail to encrypt SHA-256", e);
        }
    }
}
