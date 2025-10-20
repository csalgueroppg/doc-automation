package com.ppg.iicsdoc.model.common;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
   
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

    public static ValidationResult valid() {
        return new ValidationResult(true, new ArrayList<>(), new ArrayList<>());
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, errors, new ArrayList<>());
    }

    public static ValidationResult invalidWithWarnings(
        List<String> errors, List<String> warnings
    ) {
        return new ValidationResult(false, errors, warnings);
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }

        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }

        this.warnings.add(warning);
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
