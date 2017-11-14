package com.bitsfromspace.photos.exif.internal;

import com.bitsfromspace.photos.exif.ExifDateProvider;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * @author chris
 * @since 09/11/2017.
 */
public class ExifDataProviderImpl implements ExifDateProvider {

    private final Logger LOG = LoggerFactory.getLogger(ExifDataProviderImpl.class);

    @Override
    public Instant getDateTaken(Path file) {
        final Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file.toFile());
        } catch (Exception ex) {
            return null;
        }

        final Date dateFromTags = parseDateFromKnownTags(metadata);
        final Date autoDate = parseEarliestDate(metadata);


        if (dateFromTags == null && autoDate == null){
            return  null;
        } else if (dateFromTags == null){
            LOG.info("Cound't find date in usual metadate tags, falling back to autodate {} for file {}", autoDate, file);
        }

        return dateFromTags == null
                ? autoDate.toInstant()
                : dateFromTags.toInstant();

    }

    private Date parseEarliestDate(Metadata metadata) {
        return StreamSupport
                .stream(metadata.getDirectories().spliterator(), false)
                .flatMap(d -> d.getTags().stream().map(t-> new AbstractMap.SimpleImmutableEntry<>(t, d)))
                .map(e -> e.getValue().getDate(e.getKey().getTagType()))
                .filter(Objects::nonNull)
                .filter(this::isValidDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private Date parseAnyDate(Metadata metadata){

        Date earliestDateFound = null;
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                final Date aDate = directory.getDate(tag.getTagType());
                if (isValidDate(aDate)) {
                    if (earliestDateFound == null || aDate.before(earliestDateFound)){
                        earliestDateFound = aDate;
                    }
                }
            }
        }
        return earliestDateFound;
    }

    private Date parseDateFromKnownTags(Metadata metadata){

        if (metadata == null){
            return null;
        }

        Date dateTaken = getExifDateTime(metadata);

        if (! isValidDate(dateTaken)) {
            dateTaken = getExifDoDateTime(metadata);
        }

        if (! isValidDate(dateTaken)) {
            dateTaken = getMp4Date(metadata);
        }

        if (isValidDate(dateTaken)) {
            return dateTaken;
        }

        return null;

    }

    private boolean isValidDate(Date dateTaken) {
        if (dateTaken == null){
            return false;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTaken);
        final int year = calendar.get(Calendar.YEAR);
        return year >= 1950 && year <= 2050;
    }

    private Date getExifDateTime(Metadata metadata){
        final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (directory == null){
            return null;
        }

        return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
    }

    private Date getExifDoDateTime(Metadata metadata){
        final ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (directory == null){
            return null;
        }

        return directory.getDate(ExifIFD0Directory.TAG_DATETIME);
    }

    private Date getMp4Date(Metadata metadata){
        final Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
        if (mp4Directory == null){
            return null;
        }

        final String string = mp4Directory.getString(Mp4Directory.TAG_CREATION_TIME);
        if (string != null){
            try {
                return new SimpleDateFormat("EEE MMM dd hh:mm:ss ZZZZ yyyy").parse(string);
            } catch (ParseException ignored) {

            }
        }
        return null;
    }
}
