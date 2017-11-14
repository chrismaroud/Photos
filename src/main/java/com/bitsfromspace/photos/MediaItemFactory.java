package com.bitsfromspace.photos;

import com.bitsfromspace.photos.exif.ExifDateProvider;
import com.bitsfromspace.photos.hash.HashProvider;

import java.nio.file.Path;
import java.time.Instant;

/**
 * @author chris
 * @since 09/11/2017.
 */
@SuppressWarnings({"WeakerAccess", "UnnecessaryLocalVariable"})
public class MediaItemFactory {

    private final HashProvider hashProvider;
    private final ExifDateProvider exifDateProvider;

    public MediaItemFactory(HashProvider hashProvider, ExifDateProvider exifDateProvider) {
        this.hashProvider = hashProvider;
        this.exifDateProvider = exifDateProvider;
    }


    public MediaItem createMediaItem(Path path){

        final String hash = hashProvider.hash(path);
        final Instant instantTaken = exifDateProvider.getDateTaken(path);

        final MediaItem mediaItem = new MediaItem(hash, instantTaken, path);

        return mediaItem;
    }
}
