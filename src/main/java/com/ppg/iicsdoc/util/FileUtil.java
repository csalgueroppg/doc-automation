package com.ppg.iicsdoc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for file operations
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    
    /**
     * Calculate MD5 hash of a file
     */
    public static String calculateMD5(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file);
            byte[] hashBytes = md.digest(fileBytes);

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * Safely copy file with backup
     */
    public static void safeCopy(Path source, Path target) throws IOException {
        Path backup = null;
        if (Files.exists(target)) {
            backup = Path.of(target.toString() + ".backup");
            Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Created backup: {}", backup);
        }

        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("File copied successfully: {} -> {}", source, target);

            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        } catch (IOException e) {
            if (backup != null && Files.exists(backup)) {
                Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                log.warn("Restored from backup after copy failure");
            }

            throw e;
        }
    }

    /**
     * Read file content as string
     */
    public static String readFileAsString(Path file) throws IOException {
        return Files.readString(file);
    }

    /**
     * Write string to file
     */
    public static void writeStringToFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);

        log.debug("Written {} bytes to {}", content.length(), file);
    }

    /**
     * Get file size in human-readable format
     */
    public static String getHumanReadableString(long bytes) {
        if (bytes < 1024) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";

        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Validate file exists and is readable
     */
    public static void validateFileReadable(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("File does not exist: " + file);
        }

        if (!Files.isReadable(file)) {
            throw new IOException("File is not readable: " + file);
        }

        if (Files.isDirectory(file)) {
            throw new IOException("Path is a directory, not a file: " + file);
        }
    }

    /**
     * Clean up temporary files
     */
    public static void cleanupTempFiles(Path directory) {
        try {
            Files.walk(directory)
                .filter(p -> p.getFileName().toString().endsWith(".tmp"))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                        log.debug("Deleted temp file: {}", p);
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file: {}", p, e);
                    }
                });
        } catch (IOException e) {
            log.error("Error during temp file cleanup", e);
        }
    }
}
