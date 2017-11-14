package com.bitsfromspace.photos.hash.internal;

import com.bitsfromspace.photos.hash.HashProvider;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * @author chris
 * @since 09/11/2017.
 */
public class Sha1HashProvider implements HashProvider {

    @Override
    public String hash(Path file) {

        try (InputStream in = Files.newInputStream(file)) {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");


            final byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                sha1.update(buffer, 0, len);
            }
            return new String(sha1.digest());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }
}