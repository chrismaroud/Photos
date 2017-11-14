package com.bitsfromspace.photos.hash;

import java.nio.file.Path;

/**
 * @author chris
 * @since 09/11/2017.
 */
public interface HashProvider {

    String hash(Path file);
}
