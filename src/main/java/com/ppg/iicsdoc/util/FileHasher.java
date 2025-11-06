package com.ppg.iicsdoc.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for computing cryptographic hash values from file contents.
 * 
 * <p>
 * The {@code FileHasher} provides an efficient and secure way to calculate
 * message dists (hashes) using predefined algorithms such as {@code SHA-256},
 * {@code SHA-512}, or {@code SHA3-256}. The hashing process reads the in
 * buffered chunks to avoid leading the entire file into memory, ensuring
 * scalability for large files.
 * </p>
 * 
 * <h2>Supported Algorithms</h2>
 * <ul>
 * <li>{@code SHA-256} — Secure Hash Algorithm 2 (256-bit)</li>
 * <li>{@code SHA-512} — Secure Hash Algorithm 2 (512-bit)</li>
 * <li>{@code SHA3-256} — Secure Hash Algorithm 3 (Keccak-based, 256-bit)</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * Path file = Path.of("example.txt");
 * 
 * // Default: SHA-256
 * String sha256 = FileHasher.calculateFileHash(file);
 * System.out.println("SHA-256: " + sha256);
 * 
 * // Using a different algorithm
 * String sha3 = FileHasher.calculateFileHash(file, FileHasher.HashAlgorithm.SHA3_256);
 * System.out.println("SHA3-256: " + sha3);
 * }</pre>
 * 
 * <p>
 * The class restricts algorithms to known secure variants, preventing
 * accidental
 * or insecure use of deprecated hashed like MD5 or SHA-1.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-05
 */
public class FileHasher {

    /**
     * Enumeration of supported cryptographic hash algorithms.
     */
    public enum HashAlgorithm {
        /** SHA-256 (Secure Hash Algorithm 2, 256-bit digest) */
        SHA_256("SHA-256"),

        /** SHA-512 (Secure Hash Algorithm 2, 512-bit digest) */
        SHA_512("SHA-512"),

        /** SHA3-256 (Secure Hash Algorithm 3, Keccak-based 256-bit digest) */
        SHA3_256("SHA3-256");

        private final String name;

        HashAlgorithm(String name) {
            this.name = name;
        }

        /**
         * Returns the algorithm name as recognized by {@link MessageDigest}.
         * 
         * @return the algorithm name string
         */
        public String getName() {
            return this.name;
        }
    }

    /**
     * Calculates the cryptographic hash of a file's content using the specified
     * algorithm.
     * 
     * <p>
     * This method reads the file in 8 KB buffered chunks, updating the message
     * digest incrementally. It efficiently handles large files without consuming
     * excessive memory.
     * </p>
     * 
     * @param filePath  the path to the file
     * @param algorithm the hashing algorithm to use
     * @return the hexadecimal hash string
     * @throws IOException if reading or hashing fails
     */
    public static String calculateFileHash(Path filePath, HashAlgorithm algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getName());
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hexString.append(Character.forDigit((b >> 4) & 0xF, 16));
                hexString.append(Character.forDigit(b & 0xF, 16));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unsupported hash algorithm: " + algorithm.getName(), e);
        }
    }

    /**
     * Calculate the SHA-256 hash of a file's content.
     * 
     * <p>
     * This is a convenience overload that defaults to
     * {@link HashAlgorithm#SHA_256}.
     * </p>
     * 
     * @param filePath the path to the file
     * @return the hexadecimal SHA-256 hash string
     * @throws IOException if reading or hashing fails
     */
    public static String calculateFileHash(Path filePath) throws IOException {
        return calculateFileHash(filePath, HashAlgorithm.SHA_256);
    }

    /**
     * Verifies whether a file's hash matches an expected hash string.
     * 
     * @param filePath     the path to the file
     * @param expectedHash the expected hexadecimal hash string
     * @param algorithm    the hashing algorithm used
     * @return {@code true} if the hashes match; {@code false} otherwise
     * @throws IOException if reading or hashing fails
     */
    public static boolean verifyFileHash(
            Path filePath,
            String expectedHash,
            HashAlgorithm algorithm) throws IOException {
        String actual = calculateFileHash(filePath, algorithm);
        return MessageDigest.isEqual(actual.getBytes(), expectedHash.getBytes());
    }

    /**
     * Calculates the hash string for a text string.
     * 
     * @param text      text to calcualte the hash for
     * @param algorithm the algorithm to use for the hash
     * @return hash string for the provided text
     * @throws IOException if reading or hashing fails
     */
    public static String calculateHash(String text, HashAlgorithm algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getName());
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unsupported algorithm: " + algorithm.getName());
        }
    }

    /**
     * Calculates an HMAC using the specified algorithm and secret key.
     * 
     * @param data      the input data to hash
     * @param key       the secret key
     * @param algorithm the HMAC algorithm (e,g, "HmacSHA256")
     * @return the hexadecimal HMAC string
     * @throws IOException if hashing fails
     */
    public static String calculateHmac(byte[] data, byte[] key, String algorithm) throws IOException {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));

            byte[] result = mac.doFinal(data);
            StringBuilder hex = new StringBuilder(result.length * 2);

            for (byte b : result) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new IOException("Failed to calculate HMAC", e);
        }
    }

    /**
     * Calculates the CR32 checksum of a file's contents.
     * 
     * <p>
     * This method uses {@link java.util.zip.CRC32C} to compute a non-cryptographic
     * checksum. It is useful for detecting accidental data corruption, verifying
     * file integrity during transfer, or comparing files quickly. This is
     * <em>not</em> secure for cryptographic purposes.
     * </p>
     * 
     * <h2>Example Usage</h2>
     * 
     * <pre>{@code
     * Path filePath = Path.of("example.txt");
     * long checksum = FileHasher.calculateCRC32(filePath);
     * System.out.println("CRC32 checksum: " + checksum);
     * }</pre>
     * 
     * @param filePath the path to the file to calculate the checksum for
     * @return the CRC32 checksum as {@code long} value
     * @throws IOException if reading the file fails
     */
    public static long calculateCRC32(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath);
                CheckedInputStream cis = new CheckedInputStream(is, new CRC32())) {
            byte[] buffer = new byte[8192];

            while (cis.read(buffer) != -1) {
            }

            return cis.getChecksum().getValue();
        }
    }

    /**
     * Calculates the hash of a file and returns the result as a Base64-encoded
     * string.
     * 
     * <p>
     * This method reads the entire file into memory and computes its hash using
     * the specified {@link HashAlgorithm}. Base64 encoding is often used when
     * storing or transmitting binary hash data as text.
     * </p>
     * 
     * <h2>Example Usage</h2>
     *
     * <pre>{@code
     * Path filePath = Path.of("example.txt");
     * String hashBase64 = FileHashUtils.calculateFileHashBase64(filePath, HashAlgorithm.SHA256);
     * System.out.println("SHA-256 Base64 hash: " + hashBase64);
     * }</pre>
     * 
     * @param filePath  the path to the file to hash
     * @param algorithm the hashing algorithm to use (e.g., SHA-256, SHA-512)
     * @return the Base64-encoded hash string
     * @throws IOException              if reading the file fails
     * @throws NoSuchAlgorithmException if the specified algorithm is not supported
     */
    public static String calculateFileHashBase64(Path filePath, HashAlgorithm algorithm)
            throws IOException, NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(
                MessageDigest.getInstance(algorithm.getName())
                        .digest(Files.readAllBytes(filePath)));
    }

    /**
     * Calculates multiple hashes for a file in a single pass.
     *
     * <p>
     * This method computes the hash of the file for each {@link HashAlgorithm}
     * provided. Reading the file only once improves performance for large files
     * when multiple hash algorithms are required.
     * </p>
     *
     * <h2>Example Usage</h2>
     *
     * <pre>{@code
     * Path filePath = Path.of("example.txt");
     * Map<HashAlgorithm, String> hashes = FileHashUtils.calculateMultipleHashes(
     *         filePath,
     *         HashAlgorithm.SHA256,
     *         HashAlgorithm.SHA512);
     *
     * hashes.forEach((alg, hash) -> System.out.println(alg + ": " + hash));
     * }</pre>
     *
     * @param filePath   the path to the file to hash
     * @param algorithms the hashing algorithms to compute
     * @return a map of {@link HashAlgorithm} to the hexadecimal hash string
     * @throws IOException if reading the file fails
     */
    public static Map<HashAlgorithm, String> calculateMultipleHashes(
            Path filePath,
            HashAlgorithm... algorithms) throws IOException {
        Map<HashAlgorithm, MessageDigest> digests = new HashMap<>();
        for (HashAlgorithm alg : algorithms) {
            try {
                digests.put(alg, MessageDigest.getInstance(alg.getName()));
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("Unsupported algorithms: " + alg.getName());
            }
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                for (MessageDigest d : digests.values()) {
                    d.update(buffer, 0, bytesRead);
                }
            }
        }

        Map<HashAlgorithm, String> results = new HashMap<>();
        for (var entry : digests.entrySet()) {
            byte[] hash = entry.getValue().digest();
            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }

            results.put(entry.getKey(), hex.toString());
        }

        return results;
    }
}
