package com.ppg.iicsdoc.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BatchValidationService {
    
    private final XMLValidationService validationService;
    private final ExecutorService executorService;

    public BatchValidationService(XMLValidationService validationService) {
        this.validationService = validationService;
        this.executorService = Executors.newWorkStealingPool();
    }

    @Data
    @AllArgsConstructor
    public static class BatchValidationResult {
        private List<FileValidationResult> results;
        private long totalDurationMs;
        private int successCount;
        private int failureCount;
    }

    @Data
    @AllArgsConstructor
    public static class FileValidationResult {
        private Path file;
        private SchemaValidationResult validationResult;
        private long durationMs;
        private boolean success;
    }

    public BatchValidationResult validateBatch(List<Path> files) {
        log.info("Starting batch validation for {} files", files.size());

        long startTime = System.currentTimeMillis();
        List<CompletableFuture<FileValidationResult>> futures = files.stream()
            .map(file -> CompletableFuture.supplyAsync(() -> validateFile(file), executorService))
            .collect(Collectors.toList());

        List<FileValidationResult> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        long totalDuration = System.currentTimeMillis() - startTime;
        int successCount = (int) results.stream().filter(FileValidationResult::isSuccess).count();
        int failureCount = results.size() - successCount;

        log.info("Batch validation completed: {} succeeded, {} failed in {} ms", 
            successCount, failureCount, totalDuration);

        return new BatchValidationResult(results, totalDuration, successCount, failureCount);
    }

    private FileValidationResult validateFile(Path file) {
        long startTime = System.currentTimeMillis();
        
        try {
            SchemaValidationResult result = validationService.validateComplete(file);
            long duration = System.currentTimeMillis() - startTime;

            return new FileValidationResult(file, result, duration, result.isValid());
        } catch (Exception e) {
            log.error("Error validating file: {}", file, e);

            long duration = System.currentTimeMillis() - startTime;
            return new FileValidationResult(file, null, duration, false);
        }
    }

    public String generateBatchReport(BatchValidationResult batchResult) {
        StringBuilder report = new StringBuilder();

        report.append("# Batch Validation Report\n\n");
        report.append("## Summary\n\n");
        report.append(String.format("- **Total Files**: %d\n", batchResult.getResults().size()));
        report.append(String.format("- **Successful**: %d\n", batchResult.getSuccessCount()));
        report.append(String.format("- **Failed**: %d\n", batchResult.getFailureCount()));
        report.append(String.format("- **Total Duration**: %d ms\n", batchResult.getTotalDurationMs()));
        report.append(String.format("- **Average Duration**: %.2f ms/file\n\n", 
            (double) batchResult.getTotalDurationMs() / batchResult.getResults().size()));

        report.append("## Results\n\n");
        report.append("| File | Status | Duration | Errors | Warnings |\n");
        report.append("|------|--------|----------|--------|----------|\n");

        for (FileValidationResult result : batchResult.getResults()) {
            String status = result.isSuccess() ? "Valid" : "Invalid";
            int errors = result.getValidationResult() != null 
                ?  result.getValidationResult().getErrorCount() 
                : 0;
                
            int warnings = result.getValidationResult() != null 
                ? result.getValidationResult().getWarningCount()
                : 0;

            report.append(String.format("| %s | %s | %d ms | %d | %d |\n", 
                result.getFile().getFileName(),
                status,
                result.getDurationMs(),
                errors,
                warnings));
        }

        return report.toString();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
