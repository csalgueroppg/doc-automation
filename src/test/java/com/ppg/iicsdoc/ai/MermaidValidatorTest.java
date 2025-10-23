package com.ppg.iicsdoc.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ppg.iicsdoc.fixtures.MermaidSourceFixture;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.common.ValidationResult;

class MermaidValidatorTest {
    private MermaidValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MermaidValidator();
    }

    @Test
    void testValidateFlowChart_shouldPassWithNoErrors() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validFlowchart())
            .type(MermaidDiagram.DiagramType.FLOWCHART)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test 
    void testValidate_withValidSequenceDiagram_shouldReturnValidResult() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validSequenceDiagram())
            .type(MermaidDiagram.DiagramType.SEQUENCE)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test 
    void testValidate_withValidERDiagram_shouldReturnValidResult() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validERDiagram())
            .type(MermaidDiagram.DiagramType.ER)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test 
    void testValidate_withValidClassDiagram_shouldReturnValidResult() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validClassDiagram())
            .type(MermaidDiagram.DiagramType.CLASS)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test 
    void testValidate_withValidStateDiagram_shouldReturnValidResult() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validStateDiagram())
            .type(MermaidDiagram.DiagramType.STATE)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }

    @Test 
    void testValidate_withValidGanttDiagram_shouldReturnValidResult() {
        MermaidDiagram diagram = MermaidDiagram.builder()
            .diagramCode(MermaidSourceFixture.validGanttDiagram())
            .type(MermaidDiagram.DiagramType.GANTT)
            .build();

        ValidationResult result = validator.validate(diagram);
        assertTrue(result.isValid());
    }
}
