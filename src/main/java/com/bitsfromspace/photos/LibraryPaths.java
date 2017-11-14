package com.bitsfromspace.photos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author chris
 * @since 08/11/2017.
 */
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class LibraryPaths {
    private final String SYSTEM_PROP_LIBRARY_PATH = "libraryPath";
    private final String SYSTEM_PROP_INCOMING_PATH = "incomingPath";
    private final String SYSTEM_PROP_ERROR_PATH = "errorPath";
    private final String SYSTEM_PROP_DUPLICATE_PATH = "duplicatePath";
    private final String SYSTEM_PROP_LIB_FOLDER_FORMAT = "libraryFolderDateFormat";

    private final Path libraryPath;
    private final Path incomingPath;
    private final Path errorPath;
    private final Path duplicatePath;
    private final DateTimeFormatter libraryFolderFormat;

    public LibraryPaths(Path rootFolder) {
        libraryPath = createPath(rootFolder, "library", SYSTEM_PROP_LIBRARY_PATH);
        incomingPath = createPath(rootFolder, "incoming", SYSTEM_PROP_INCOMING_PATH);
        errorPath = createPath(rootFolder, "error", SYSTEM_PROP_ERROR_PATH);
        duplicatePath = createPath(rootFolder, "duplicate", SYSTEM_PROP_DUPLICATE_PATH);

        final String libraryFolderFormatOverride = System.getProperty(SYSTEM_PROP_LIB_FOLDER_FORMAT);
        libraryFolderFormat = libraryFolderFormatOverride == null
                ? DateTimeFormatter.ofPattern("yyyy/MM").withZone(ZoneId.systemDefault())
                : DateTimeFormatter.ofPattern(libraryFolderFormatOverride).withZone(ZoneId.systemDefault());
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Path createPath(Path rootFolder, String subFolder, String overrideSystemProperty) {
        final String overridePath = System.getProperty(overrideSystemProperty);
        final Path path = overridePath == null
                ? rootFolder.resolve(subFolder)
                : Paths.get(overridePath);


        return path;
    }

    public Path getLibraryPath(Instant date) {
        final String subfolder = libraryFolderFormat.format(date);
        return libraryPath.resolve(subfolder);
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public Path getIncomingPath() {
        return incomingPath;
    }

    public Path getErrorPath() {
        return errorPath;
    }

    public Path getDuplicatePath() {
        return duplicatePath;
    }
}
