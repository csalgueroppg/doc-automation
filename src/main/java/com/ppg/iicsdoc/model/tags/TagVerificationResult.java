package com.ppg.iicsdoc.model.tags;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVerificationResult {
    
    private int totalTags;
    private int validTags;
    private int outdatedTags;
    private int missingTags;
    private int errorTags;
    private List<Tag> problematicTags;

    public boolean isAllValid() {
        return totalTags > 0 && validTags == totalTags;
    }

    public double getSuccessRate() {
        if (totalTags == 0) {
            return 1.0;
        }

        return (double) validTags / totalTags;
    }
}
