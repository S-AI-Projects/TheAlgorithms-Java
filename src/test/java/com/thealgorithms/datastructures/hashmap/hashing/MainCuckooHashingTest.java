package com.thealgorithms.datastructures.hashmap.hashing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression contract tests for {@link MainCuckooHashing}.
 */
public class MainCuckooHashingTest {

    private static final String MENU =
        "_________________________" + System.lineSeparator()
        + "Enter your Choice :" + System.lineSeparator()
        + "1. Add Key" + System.lineSeparator()
        + "2. Delete Key" + System.lineSeparator()
        + "3. Print Table" + System.lineSeparator()
        + "4. Exit" + System.lineSeparator()
        + "5. Search and print key index" + System.lineSeparator()
        + "6. Check load factor" + System.lineSeparator()
        + "7. Rehash Current Table" + System.lineSeparator();

    private final InputStream standardIn = System.in;
    private final PrintStream standardOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() {
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setIn(standardIn);
        System.setOut(standardOut);
    }

    /**
     * Smallest valid input fixture: choice 4 (Exit).
     * Preconditions established at:
     * - MainCuckooHashing.java line 22 ("4. Exit")
     * - MainCuckooHashing.java lines 47-49 (case 4: scan.close(); return;)
     */
    @Test
    void testSmallestValidInputExitDirectly() {
        // Literal input: "4\n" (smallest valid sequence to start and cleanly terminate)
        String literalInput = "4" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(literalInput.getBytes()));

        // Independently calculated expected output: menu printed once, followed by clean termination.
        String expectedOutput = MENU;

        assertDoesNotThrow(() -> MainCuckooHashing.main(new String[0]));

        String actualOutput = outputStreamCaptor.toString();
        assertEquals(expectedOutput, actualOutput, "Smallest valid input (exit) must only display initial menu and terminate.");
    }

    /**
     * Ordinary input fixture:
     * 1. Add key 42 (case 1)
     * 2. Search key 42 and print index (case 5)
     * 3. Check load factor (case 6)
     * 4. Exit (case 4)
     *
     * Preconditions established at:
     * - MainCuckooHashing.java line 19 & lines 30-34 (choice 1: Add Key, requires integer key)
     * - MainCuckooHashing.java line 23 & lines 51-55 (choice 5: Search key, requires integer key)
     * - MainCuckooHashing.java line 24 & lines 57-59 (choice 6: Check load factor)
     * - MainCuckooHashing.java line 22 & lines 47-49 (choice 4: Exit)
     */
    @Test
    void testOrdinaryInputWorkflow() {
        // Literal inputs: add 42, find 42, check load factor, exit
        String literalInput = "1" + System.lineSeparator()
            + "42" + System.lineSeparator()
            + "5" + System.lineSeparator()
            + "42" + System.lineSeparator()
            + "6" + System.lineSeparator()
            + "4" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(literalInput.getBytes()));

        // Independently calculated expected outputs:
        // Table size = 7 (MainCuckooHashing.java line 13: new HashMapCuckooHashing(7))
        // 42 % 7 = 0; index = 0
        // Size after 1 insert = 1; load factor = 1.0 / 7.0 = 0.142857..., formatted %.2f -> 0.14
        String expectedOutput = MENU
            + "Enter the Key: " + System.lineSeparator()
            + MENU
            + "Enter the Key to find and print:  " + System.lineSeparator()
            + "Key: 42 is at index: 0" + System.lineSeparator() + System.lineSeparator()
            + MENU
            + String.format("Load factor is: %.2f%n", 1.0 / 7.0)
            + MENU;

        assertDoesNotThrow(() -> MainCuckooHashing.main(new String[0]));

        String actualOutput = outputStreamCaptor.toString();
        assertEquals(expectedOutput, actualOutput, "Ordinary workflow must match independently calculated outputs for add, search, and load factor.");
    }

    /**
     * Domain boundary fixture: repeated value (duplicate insertion).
     * Preconditions established at:
     * - MainCuckooHashing.java line 19 & lines 30-34 (choice 1: Add Key)
     * - HashMapCuckooHashing.java lines 84-86 (duplicate key precondition check throws IllegalArgumentException)
     */
    @Test
    void testDomainBoundaryDuplicateKeyThrowsException() {
        // Literal input: insert 10, then attempt to insert 10 again
        String literalInput = "1" + System.lineSeparator()
            + "10" + System.lineSeparator()
            + "1" + System.lineSeparator()
            + "10" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(literalInput.getBytes()));

        String expectedOutputBeforeFailure = MENU
            + "Enter the Key: " + System.lineSeparator()
            + MENU
            + "Enter the Key: " + System.lineSeparator();

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> MainCuckooHashing.main(new String[0]),
            "Inserting duplicate key must throw IllegalArgumentException."
        );

        assertEquals("Key already exists; duplicates are not allowed.", thrown.getMessage());
        String actualOutput = outputStreamCaptor.toString();
        assertEquals(expectedOutputBeforeFailure, actualOutput, "Output before failure must match prompts leading up to duplicate insertion.");
    }

    /**
     * Domain boundary fixture: unexpected menu choice value.
     * Preconditions established at:
     * - MainCuckooHashing.java lines 65-67 (default: throw new IllegalArgumentException("Unexpected value: " + choice);)
     */
    @Test
    void testDomainBoundaryUnexpectedMenuChoiceThrowsException() {
        // Literal input: choice 8 is out of supported menu bounds [1, 7]
        String literalInput = "8" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(literalInput.getBytes()));

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> MainCuckooHashing.main(new String[0]),
            "Invalid menu choice must trigger default branch IllegalArgumentException."
        );

        assertEquals("Unexpected value: 8", thrown.getMessage());
        assertEquals(MENU, outputStreamCaptor.toString(), "Output before exception must match single menu display.");
    }

    /**
     * Observable mutation fixture:
     * Tests that key insertion mutates the underlying hash table and is observably reflected
     * when printing the table contents.
     * Preconditions established at:
     * - MainCuckooHashing.java line 19 & lines 30-34 (choice 1: Add Key)
     * - MainCuckooHashing.java line 21 & lines 42-45 (choice 3: Print Table)
     * - MainCuckooHashing.java line 22 & lines 47-49 (choice 4: Exit)
     */
    @Test
    void testObservableMutationViaTableDisplay() {
        // Literal input: insert key 5 (5 % 7 = 5), display table, exit
        String literalInput = "1" + System.lineSeparator()
            + "5" + System.lineSeparator()
            + "3" + System.lineSeparator()
            + "4" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(literalInput.getBytes()));

        assertDoesNotThrow(() -> MainCuckooHashing.main(new String[0]));

        String actualOutput = outputStreamCaptor.toString();
        assertTrue(actualOutput.contains("Bucket 5: 5"), "Observable mutation: bucket 5 must contain inserted key 5.");
        assertTrue(actualOutput.contains("Bucket 0: Empty"), "Unoccupied buckets must remain Empty.");
    }

    /**
     * Reflection fixture for private constructor contract.
     * Preconditions established at:
     * - MainCuckooHashing.java lines 6-7 (private MainCuckooHashing() constructor)
     */
    @Test
    void testPrivateConstructorContract() throws Exception {
        Constructor<MainCuckooHashing> constructor = MainCuckooHashing.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        MainCuckooHashing instance = constructor.newInstance();
        assertNotNull(instance, "Private constructor must be instantiable via reflection.");
    }
}
