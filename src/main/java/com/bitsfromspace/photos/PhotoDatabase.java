package com.bitsfromspace.photos;

import com.bitsfromspace.utils.LockUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.bitsfromspace.utils.LockUtils.asClosable;

/**
 * @author chris
 * @since 09/11/2017.
 */
@SuppressWarnings("WeakerAccess")
public class PhotoDatabase {

    private final Map<String, MediaItem> db;
    private final ReadWriteLock rwLock;

    public PhotoDatabase() {
        db = new HashMap<>();
        rwLock = new ReentrantReadWriteLock();
    }

    /*
        Returns true if added, false if already exists.
     */
    public boolean add(MediaItem file) {
        try (LockUtils.ClosableLock ignored = asClosable(rwLock.writeLock())) {
            return db.putIfAbsent(file.getHash(), file) == null;
        }
    }


    public MediaItem get(String hash) {
        try (LockUtils.ClosableLock ignored = asClosable(rwLock.readLock())) {
            return db.get(hash);
        }
    }

    public boolean containsHash(String hash) {
        try (LockUtils.ClosableLock ignored = asClosable(rwLock.readLock())){
            return db.containsKey(hash);
        }
    }

    public int size() {
        try (LockUtils.ClosableLock ignored = asClosable(rwLock.readLock())){
            return db.size();
        }
    }
}
