package com.bitsfromspace.utils;

import java.util.concurrent.locks.Lock;

/**
 * @author chris
 * @since 09/11/2017.
 */
public interface LockUtils {

    static ClosableLock asClosable(Lock lock) {
        lock.lock();
        return lock::unlock;
    }

    interface ClosableLock extends AutoCloseable {
        @Override
        void close();
    }
}
