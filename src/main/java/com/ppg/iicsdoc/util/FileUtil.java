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
 * A utility class that provides common file operations such as reading,
 * writing, copying with backup, hashing, and cleanup. All methods are static
 * and designed for safe and reusable use in production environments.
 * 
 * <p>
 * This class handles edge cases like backup restoration on copy failure and
 * directory creation for writes.
 * </p>
 * 
 * <p>
 * Logging is integrated for traceability.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Computes the MD5 hash of the contents of the specified file.
     * 
     * <p>
     * This method reads the entire file into memory, so it may not be suitable
     * for very large files.
     * </p>
     * 
     * @param file the path to the file whose MD5 hash is to be calculated
     * @return a hexadecimal string representing the MD5 hash
     * @throws IOException      if an I/O error occurs while reading the file
     * @throws RuntimeException if the MD5 algorithm is not available
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
     * Copies a file from the source path to the target path with backup
     * protection.
     * 
     * <p>
     * If the target file already exists, it is backed up with a ".backup" suffix
     * If the copy operation fails, the original target file is restored from the
     * backup
     * </p>
     * 
     * @param source the path to the source file
     * @param target the path to the target file
     * @throws IOException if an error occurs during copy or backup restoration
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
     * Reads the entire contents of a file into a string using the platform's
     * default charset.
     * 
     * @param file the path to the file to read
     * @return the file contents as a string
     * @throws IOException if an error occurs while reading the file
     */
    public static String readFileAsString(Path file) throws IOException {
        return Files.readString(file);
    }

    /**
     * Writes the specified string content to a file.
     * 
     * <p>
     * If the parent directories of the file do not exist, they are created
     * automatically.
     * </p>
     * 
     * @param file    the path to the file to write
     * @param content the string content to write
     * @throws IOException if an error occurs while writing to the file
     */
    public static void writeStringToFile(Path file, String content)
            throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);

        log.debug("Written {} bytes to {}", content.length(), file);
    }

    /**
     * Converts a byte count into a human-readable string using binary units.
     * 
     * <p>
     * For example, 1536 bytes will be formatted as "1.5 KB"
     * </p>
     * 
     * @param bytes the number of bytes
     * @return a formatted string representing the size in human-readable form
     */
    public static String getHumanReadableString(long bytes) {
        if (bytes < 1024)
            return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";

        return "%.1f %sB".formatted(bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Validates that the specified path points to a readable,
     * *
     * 
     * @param file the path to validate
     * @throws IOException if the file does not exist, is not readable, or is a
     *                     directory.
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
     * Deletes all temporary files (files ending with ".tmp") within the
     * specified directory.
     * 
     * <p>
     * This method walks the directory recursively and logs any deletion
     * failures.
     * </p>
     * 
     * @param directory the root directory to scan for temporary files
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
