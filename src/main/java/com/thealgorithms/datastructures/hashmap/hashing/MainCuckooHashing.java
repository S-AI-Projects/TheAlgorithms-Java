package com.thealgorithms.datastructures.hashmap.hashing;

import java.util.Scanner;

/**
 * Interactive driver for {@link HashMapCuckooHashing}.
 *
 * <p>Contract:
 * <ul>
 *   <li><b>Input:</b> Stream of whitespace-delimited integer tokens from standard input ({@link System#in}).
 *     Supported menu choices:
 *     <ul>
 *       <li>1 (Add key): reads integer {@code key}; precondition: key does not exist in table.</li>
 *       <li>2 (Delete key): reads integer {@code key}; precondition: key exists in table.</li>
 *       <li>3 (Print table): outputs table slots to standard output.</li>
 *       <li>4 (Exit): terminates the program cleanly and closes scanner.</li>
 *       <li>5 (Search key): reads integer {@code key}; prints 0-based bucket index.</li>
 *       <li>6 (Load factor): prints current load factor formatted to 2 decimal places.</li>
 *       <li>7 (Rehash): manually triggers rehashing and doubling table capacity.</li>
 *     </ul>
 *   </li>
 *   <li><b>Output:</b> Writes menus, interactive prompts, table displays, search results, and load factors to standard output ({@link System#out}).</li>
 *   <li><b>Mutations:</b> Mutates the internal {@link HashMapCuckooHashing} instance (initial capacity 7) and advances standard input stream state.</li>
 *   <li><b>Preconditions:</b> Standard input must supply integer tokens matching the expected menu workflow.</li>
 *   <li><b>Exceptions:</b> Throws {@link IllegalArgumentException} when an unknown choice is entered (line 66)
 *       or when underlying operations reject duplicate key additions or missing key deletions.</li>
 * </ul>
 */
public final class MainCuckooHashing {
    private MainCuckooHashing() {
    }

    public static void main(String[] args) {
        int choice;
        int key;

        HashMapCuckooHashing h = new HashMapCuckooHashing(7);
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.println("_________________________");
            System.out.println("Enter your Choice :");
            System.out.println("1. Add Key");
            System.out.println("2. Delete Key");
            System.out.println("3. Print Table");
            System.out.println("4. Exit");
            System.out.println("5. Search and print key index");
            System.out.println("6. Check load factor");
            System.out.println("7. Rehash Current Table");

            choice = scan.nextInt();

            switch (choice) {
            case 1:
                System.out.println("Enter the Key: ");
                key = scan.nextInt();
                h.insertKey2HashTable(key);
                break;

            case 2:
                System.out.println("Enter the Key delete:  ");
                key = scan.nextInt();
                h.deleteKeyFromHashTable(key);
                break;

            case 3:
                System.out.println("Print table:\n");
                h.displayHashtable();
                break;

            case 4:
                scan.close();
                return;

            case 5:
                System.out.println("Enter the Key to find and print:  ");
                key = scan.nextInt();
                System.out.println("Key: " + key + " is at index: " + h.findKeyInTable(key) + "\n");
                break;

            case 6:
                System.out.printf("Load factor is: %.2f%n", h.checkLoadFactor());
                break;

            case 7:
                h.reHashTableIncreasesTableSize();
                break;

            default:
                throw new IllegalArgumentException("Unexpected value: " + choice);
            }
        }
    }
}
