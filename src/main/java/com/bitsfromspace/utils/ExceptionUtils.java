package com.bitsfromspace.utils;

import java.util.concurrent.Callable;

/**
 * @author chris
 * @since 09/11/2017.
 */
public interface ExceptionUtils {

    static <T> T unchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
