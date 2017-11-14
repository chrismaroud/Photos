package com.bitsfromspace.photos;

import com.bitsfromspace.photos.exif.ExifDateProvider;
import com.bitsfromspace.photos.exif.internal.ExifDataProviderImpl;
import com.bitsfromspace.photos.hash.HashProvider;
import com.bitsfromspace.photos.hash.internal.Sha1HashProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;

import static com.bitsfromspace.utils.ExceptionUtils.unchecked;

/**
 * @author chris
 * @since 09/11/2017.
 */
public class PhotosApp {

    private final static Logger LOG = LoggerFactory.getLogger(PhotosApp.class);

    private final Library library;

    private PhotosApp(Library library) {
        this.library = library;
    }

    private void start() {
        LOG.info("PhotosApp Initializing Library");
        library.init();
        LOG.info("PhotosApp Initialied, library has {} items", library.size());
        LOG.info("PhotosApp Starting watch service");
        startWatchService();
        LOG.info("PhotosApp initialized");
    }

    public static void main(String[] args) {
        if (args.length != 1){
            System.err.println("Usage: PhotosApp <RootDir>, where root dir is the parent folder of <Library>, <Incoming>, <Error> and <Duplicates>");
            System.exit(1);
        }

        final Path rootPath = Paths.get(args[0]);
        verifyFolder(rootPath);

        final LibraryPaths libraryPaths = new LibraryPaths(rootPath);
        verifyFolder(libraryPaths.getLibraryPath());
        verifyFolder(libraryPaths.getErrorPath());
        verifyFolder(libraryPaths.getIncomingPath());
        verifyFolder(libraryPaths.getDuplicatePath());

        final HashProvider hashProvider = new Sha1HashProvider();
        final ExifDateProvider exifDateProvider = new ExifDataProviderImpl();
        final MediaItemFactory mediaItemFactory = new MediaItemFactory(hashProvider, exifDateProvider);
        final PhotoDatabase database = new PhotoDatabase();
        final Library library = new Library(database, libraryPaths, mediaItemFactory);

        final PhotosApp app = new PhotosApp(library);

        app.start();
    }

    private static void verifyFolder(Path folder) {
        if (! Files.exists(folder)){
            unchecked(() -> Files.createDirectories(folder));
        }
        if (! Files.isDirectory(folder)) {
            System.err.println("Library path is not a directory: " + folder);
            System.exit(2);
        }
    }

    private void startWatchService(){

        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(5000);
                        library.processIncomingFolder();
                    } catch (InterruptedException intEx) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                LOG.info("Watch service stopped");
            }

        }).start();
    }

//
}
