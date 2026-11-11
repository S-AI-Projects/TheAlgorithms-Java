package com.thealgorithms.maths;

/**
 * Multiplies two complex numbers represented as strings in the form "a+bi".
 * Supports negative values and validates input format.
 */
public final class ComplexNumberMultiply {

    private ComplexNumberMultiply() {
    }

    private static int[] parse(String num) {
        if (num == null || !num.matches("-?\\d+\\+-?\\d+i")) {
            throw new IllegalArgumentException("Invalid complex number format: " + num);
        }

        String[] parts = num.split("\\+");
        int real = Integer.parseInt(parts[0]);
        int imaginary = Integer.parseInt(parts[1].replace("i", ""));
        return new int[] {real, imaginary};
    }

    /**
     * Multiplies two complex numbers formatted as {@code "a+bi"}.
     *
     * <p>Input contract: both {@code num1} and {@code num2} must be non-null strings matching
     * {@code -?\d+\+-?\d+i} with integer components (for example {@code "0+0i"} or
     * {@code "1+-2i"}); otherwise {@link IllegalArgumentException} is thrown.
     *
     * <p>Output contract: pure function returning {@code "<real>+<imaginary>i"} where
     * {@code real = a*c - b*d} and {@code imaginary = a*d + b*c}, preserving negative signs
     * directly (for example {@code "0+-2i"}).
     *
     * @param num1 first complex number in {@code "a+bi"} form
     * @param num2 second complex number in {@code "a+bi"} form
     * @return product formatted as {@code "real+imaginaryi"}
     * @throws IllegalArgumentException if either input is null or does not match {@code -?\d+\+-?\d+i}
     */
    public static String multiply(String num1, String num2) {
        int[] a = parse(num1);
        int[] b = parse(num2);

        int real = a[0] * b[0] - a[1] * b[1];
        int imaginary = a[0] * b[1] + a[1] * b[0];

        return real + "+" + imaginary + "i";
    }
}
