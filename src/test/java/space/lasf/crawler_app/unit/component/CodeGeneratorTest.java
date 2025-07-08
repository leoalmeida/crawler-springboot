package space.lasf.crawler_app.unit.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import space.lasf.crawler_app.component.CodeGenerator;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {

    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        codeGenerator = new CodeGenerator();
    }

    @Test
    @DisplayName("Should generate a non-null code with the correct length (8)")
    void generateRandomCode_shouldHaveCorrectLength() {
        String code = codeGenerator.generateRandomCode();
        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @RepeatedTest(10)
    @DisplayName("Should generate a code containing only allowed characters")
    void generateRandomCode_shouldContainOnlyAllowedChars() {
        String code = codeGenerator.generateRandomCode();
        assertTrue(
                code.matches("[a-zA-Z0-9]{8}"),
                "Generated code '" + code + "' should only contain alphanumeric characters"
        );
    }
}