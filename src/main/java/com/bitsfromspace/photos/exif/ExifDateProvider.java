package com.bitsfromspace.photos.exif;

import java.nio.file.Path;
import java.time.Instant;

/**
 * @author chris
 * @since 09/11/2017.
 */
public interface ExifDateProvider {

    Instant getDateTaken(Path file);
}
