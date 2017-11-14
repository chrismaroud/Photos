package com.bitsfromspace.photos;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * @author chris
 * @since 09/11/2017.
 */
@SuppressWarnings("WeakerAccess")
public class MediaItem {

    private final String hash;
    private final Instant instantTaken;
    private final Path path;

    public MediaItem(String hash, Instant instantTaken, Path path) {
        this.hash = hash;
        this.instantTaken = instantTaken;
        this.path = path;
    }

    public Instant getInstantTaken() {
        return instantTaken;
    }

    public Path getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public MediaItem withPath(Path newPath) {
        return new MediaItem(hash, instantTaken, newPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaItem mediaItem = (MediaItem) o;
        return Objects.equals(getHash(), mediaItem.getHash()) &&
                Objects.equals(getInstantTaken(), mediaItem.getInstantTaken()) &&
                Objects.equals(getPath(), mediaItem.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHash(), getInstantTaken(), getPath());
    }
}
