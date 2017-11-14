package com.bitsfromspace.photos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author chris
 * @since 09/11/2017.
 */
public interface DirectoryScanner {

   Logger LOG = LoggerFactory.getLogger(DirectoryScanner.class);


    static void scan(Path directory, Callback callback) {
        LOG.info("Scanning {}", directory);
        try {
            Files.walk(directory)
                 .filter(Files::isRegularFile)
                 .forEach(callback::found);

        } catch (IOException ioEx){
            throw new IOError(ioEx);
        }
    }


    interface Callback {
        void found(Path file);
    }
}
