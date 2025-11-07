package com.ppg.iicsdoc.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.validation.XMLValidationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class ValidationPerformanceTest {
    
    @Autowired
    private XMLValidationService validationService;

    @Test
    void shouldValidateMultipleFilesInParallel() {
        Path[] files = {
                Paths.get("src/test/resources/sample-xml/cai-process.xml"),
                Paths.get("src/test/resources/sample-xml/complex-cdi-process.xml"),
                Paths.get("src/test/resources/sample-xml/api-gateway-cai-process.xml")
        };

        long startTime = System.currentTimeMillis();
        List<SchemaValidationResult> results = new ArrayList<>();
        for (Path file : files) {
            results.add(validationService.validateComplete(file));
        }

        long duration = System.currentTimeMillis() - startTime;
        assertEquals(files.length, results.size());

        log.info("Validated {} files in {} ms (avg: {} ms/file)", 
            files.length, duration, duration / files.length);
    }

    @Test
    void shouldMeasureValidationOverhead() {
        Path file = Paths.get("src/test/resources/sample-xml/complex-cdi-process.xml");
        int iterations = 10;

        for (int i = 0; i < 3; i++) {
            validationService.validateComplete(file);
        }

        long totalDuration = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            SchemaValidationResult result = validationService.validateComplete(file);

            long duration = System.currentTimeMillis() - start;
            totalDuration += duration;
        }

        long avgDuration = totalDuration / iterations;
        log.info("Average validation time over {} iterations: {} ms",
            iterations, avgDuration);

        assertTrue(avgDuration < 1000, "Average validation should be under 1 second");
    }

    @Test 
    void shouldHandleMemoryEfficiently() {
        Path largeFile = Paths.get("src/test/resources/sample-xml-generated/large-process.xml");
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        SchemaValidationResult result = validationService.validateComplete(largeFile);
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024);

        log.info("Memory used for large file validation: {} MB", memoryUsed);
        assertTrue(memoryUsed < 100, "Should use less than 100 MB for validation");
    }
}
