package com.bitsfromspace.photos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.bitsfromspace.utils.ExceptionUtils.unchecked;

/**
 * @author chris
 * @since 10/11/2017.
 */
@SuppressWarnings("WeakerAccess")
public class Library {

    private final static Logger LOG = LoggerFactory.getLogger(Library.class);

    private final PhotoDatabase database;
    private final LibraryPaths libraryPaths;
    private final MediaItemFactory mediaItemFactory;
    private final Map<Path, Long> incomingFileSizes;

    public Library(PhotoDatabase database, LibraryPaths libraryPaths, MediaItemFactory mediaItemFactory) {
        this.database = database;
        this.libraryPaths = libraryPaths;
        this.mediaItemFactory = mediaItemFactory;
        incomingFileSizes = new HashMap<>();
    }

    public void processIncomingFolder() {

        try {
            Files.walk(libraryPaths.getIncomingPath())
                    .filter(Files::isRegularFile)
                    .forEach(this::processIncomingFile);

            //remove empty directories:

            //noinspection ResultOfMethodCallIgnored // ignore delete status
            Files.walk(libraryPaths.getIncomingPath())
                    .filter(Files::isDirectory)
                    .filter(p -> p != libraryPaths.getIncomingPath())
                    .sorted(Comparator.reverseOrder())
                    .filter(this::isEmptyDir) // empty
                    .map(Path::toFile)
                    .peek(p -> LOG.info("Deleting processed incoming subdirectory {}", p))
                    .forEach(File::delete);
        } catch (Exception ex){
            LOG.error("Error processing incoming folder: " + libraryPaths.getIncomingPath(), ex);
        }


    }

    private boolean isEmptyDir(Path path) {
        return unchecked(() -> ! Files.newDirectoryStream(path).iterator().hasNext());
    }

    public void init() {
        try {
            Files.walk(libraryPaths.getLibraryPath())
                    .peek(p -> {
                        if (Files.isDirectory(p)){
                            LOG.info("Reading {}", p);
                        }
                    })
                    .filter(Files::isRegularFile)
                    .map(mediaItemFactory::createMediaItem)
                    .filter(m -> !database.add(m))
                    .forEach(m -> LOG.error(
                            "Duplicate file in Library: '{}' & '{}' ",
                            m,
                            database.get(m.getHash()))
                    );
        } catch (IOException ioEx){
            throw new IOError(ioEx);
        }
    }

    private void processIncomingFile(Path sourceFile) {
        if (! isReady(sourceFile)){
            return;
        }

        final MediaItem mediaItem = mediaItemFactory.createMediaItem(sourceFile);
        if (mediaItem.getInstantTaken() == null){
            LOG.error("No Exif data available, moving to error library folder: {}", sourceFile);
            move(libraryPaths.getIncomingPath(), sourceFile, libraryPaths.getErrorPath());
            return;
        }

        if (database.containsHash(mediaItem.getHash())){
            final Path duplicatedFile = database.get(mediaItem.getHash()).getPath();
            LOG.error("Duplicate file: {} --> {}", sourceFile, duplicatedFile);
            move(libraryPaths.getIncomingPath(), sourceFile, libraryPaths.getDuplicatePath());
            return;
        }

        final Path targetLibraryPath = libraryPaths.getLibraryPath(mediaItem.getInstantTaken());
        if (! Files.exists(targetLibraryPath)){
            LOG.info("Creating library folder {}", targetLibraryPath);
            unchecked(()->Files.createDirectories(targetLibraryPath));
        }

        LOG.info("Importing into library {} --> {}", sourceFile, targetLibraryPath);
        final Path movedFile = move(sourceFile.getParent(), sourceFile, targetLibraryPath);
        database.add(mediaItem.withPath(movedFile));
    }

    private boolean isReady(Path sourceFile) {
        return unchecked(() -> {
            final long size = Files.size(sourceFile);
            final Long expectedSize = incomingFileSizes.put(sourceFile, size);
            if (expectedSize == null || expectedSize != size) {
                return false;
            } else {
                incomingFileSizes.remove(sourceFile);
                return true;
            }
        });
    }

    private Path move(Path sourceFolder, Path sourceFile, Path targetFolder) {
        final Path relativeFolder = sourceFolder.relativize(sourceFile.getParent());
        final Path targetPath = targetFolder.resolve(relativeFolder);

        return unchecked( () -> {
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            final String filename = sourceFile.getFileName().toString();
            int duplicateNameCount = 0;
            Path targetFile;
            do {
                final int extPos = filename.lastIndexOf('.');
                if (duplicateNameCount++ == 0) {
                    targetFile = targetPath.resolve(filename);
                } else if (extPos == -1) {
                    targetFile = targetPath.resolve(filename + " (" + duplicateNameCount + ")");
                } else {
                    targetFile = targetPath.resolve(filename.substring(0, extPos) + " (" + duplicateNameCount + ")" + filename.substring(extPos));
                }
            } while (Files.exists(targetFile));

            Files.move(sourceFile, targetFile);
            return targetFile;
        });

    }


    public int size() {
        return database.size();
    }
}
