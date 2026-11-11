package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link Mandelbrot#getImage(int, int, double, double, double, int, boolean)}.
 *
 * <p>Source anchors ({@code Mandelbrot.java}, {@code main} lines 32-47):
 * <ul>
 * <li>{@code blackAndWhiteImage = getImage(800, 600, -0.6, 0, 3.2, 50, false)}</li>
 * <li>{@code blackAndWhiteImage.getRGB(0, 0) == new Color(255, 255, 255).getRGB()} (line 35)</li>
 * <li>{@code blackAndWhiteImage.getRGB(400, 300) == new Color(0, 0, 0).getRGB()} (line 38)</li>
 * <li>{@code coloredImage = getImage(800, 600, -0.6, 0, 3.2, 50, true)}</li>
 * <li>{@code coloredImage.getRGB(0, 0) == new Color(255, 0, 0).getRGB()} (line 44)</li>
 * <li>{@code coloredImage.getRGB(400, 300) == new Color(0, 0, 0).getRGB()} (line 47)</li>
 * </ul>
 */
public class MandelbrotTest {

    private static void logCase(String id, String input, Object expected, Object actual) {
        System.out.println("[R45-T016] " + id + " | input=" + input
            + " | expected=" + expected + " | actual=" + actual);
    }

    /**
     * Executable source anchors from {@code Mandelbrot.main} (lines 32-47) plus neighboring pixel
     * {@code (1, 0)} (figure coordinates {@code (-2.196, -1.2)}, diverging at step 0).
     */
    @Test
    public void sourceAnchorsAndNeighboringPixelMatchExpectedRgb() {
        BufferedImage bw = Mandelbrot.getImage(800, 600, -0.6, 0, 3.2, 50, false);
        assertEquals(800, bw.getWidth());
        assertEquals(600, bw.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, bw.getType());

        int white = new Color(255, 255, 255).getRGB();
        int black = new Color(0, 0, 0).getRGB();
        int red = new Color(255, 0, 0).getRGB();

        int bw00 = bw.getRGB(0, 0);
        logCase("anchor-bw-0-0", "800x600 bw (0,0)", white, bw00);
        assertEquals(white, bw00);

        int bw400300 = bw.getRGB(400, 300);
        logCase("anchor-bw-400-300", "800x600 bw (400,300)", black, bw400300);
        assertEquals(black, bw400300);

        int bw10 = bw.getRGB(1, 0);
        logCase("neighbor-bw-1-0", "800x600 bw (1,0)", white, bw10);
        assertEquals(white, bw10);

        BufferedImage colored = Mandelbrot.getImage(800, 600, -0.6, 0, 3.2, 50, true);
        int col00 = colored.getRGB(0, 0);
        logCase("anchor-colored-0-0", "800x600 colored (0,0)", red, col00);
        assertEquals(red, col00);

        int col400300 = colored.getRGB(400, 300);
        logCase("anchor-colored-400-300", "800x600 colored (400,300)", black, col400300);
        assertEquals(black, col400300);

        int col10 = colored.getRGB(1, 0);
        logCase("neighbor-colored-1-0", "800x600 colored (1,0)", red, col10);
        assertEquals(red, col10);
    }

    /**
     * Independently hand-calculated {@code 2x2} image centered at {@code (0, 0)} with
     * {@code figureWidth = 2.0, maxStep = 5}:
     * <ul>
     * <li>{@code (0, 0)} -> {@code (-1.0, -1.0)} breaks at step 1 -> {@code distance = 0.25} ->
     * color {@code (127, 255, 0)}</li>
     * <li>{@code (0, 1)} -> {@code (-1.0, 0.0)} period-2 cycle -> {@code distance = 1.0} ->
     * black {@code (0, 0, 0)}</li>
     * <li>{@code (1, 1)} -> {@code (0.0, 0.0)} fixed point -> {@code distance = 1.0} ->
     * black {@code (0, 0, 0)}</li>
     * </ul>
     */
    @Test
    public void independentlyCalculatedSmallGridMatchesExactPixelValues() {
        BufferedImage bw = Mandelbrot.getImage(2, 2, 0.0, 0.0, 2.0, 5, false);
        BufferedImage colored = Mandelbrot.getImage(2, 2, 0.0, 0.0, 2.0, 5, true);

        int white = new Color(255, 255, 255).getRGB();
        int black = new Color(0, 0, 0).getRGB();
        int expectedColor00 = new Color(127, 255, 0).getRGB();

        logCase("grid-2x2-bw-0-0", "2x2 bw (0,0)", white, bw.getRGB(0, 0));
        assertEquals(white, bw.getRGB(0, 0));

        logCase("grid-2x2-colored-0-0", "2x2 colored (0,0)", expectedColor00, colored.getRGB(0, 0));
        assertEquals(expectedColor00, colored.getRGB(0, 0));

        logCase("grid-2x2-colored-0-1", "2x2 colored (0,1)", black, colored.getRGB(0, 1));
        assertEquals(black, colored.getRGB(0, 1));

        logCase("grid-2x2-colored-1-1", "2x2 colored (1,1)", black, colored.getRGB(1, 1));
        assertEquals(black, colored.getRGB(1, 1));
    }

    /**
     * Parameter validation contract (lines 76-86): non-positive dimensions or {@code maxStep}
     * throw {@link IllegalArgumentException} with specific messages.
     */
    @Test
    public void invalidArgumentsThrowIllegalArgumentException() {
        IllegalArgumentException badWidth = assertThrows(
            IllegalArgumentException.class,
            () -> Mandelbrot.getImage(0, 10, 0, 0, 2.0, 10, false));
        logCase("invalid-width", "imageWidth=0",
            "imageWidth should be greater than zero", badWidth.getMessage());
        assertEquals("imageWidth should be greater than zero", badWidth.getMessage());

        IllegalArgumentException badHeight = assertThrows(
            IllegalArgumentException.class,
            () -> Mandelbrot.getImage(10, 0, 0, 0, 2.0, 10, false));
        logCase("invalid-height", "imageHeight=0",
            "imageHeight should be greater than zero", badHeight.getMessage());
        assertEquals("imageHeight should be greater than zero", badHeight.getMessage());

        IllegalArgumentException badStep = assertThrows(
            IllegalArgumentException.class,
            () -> Mandelbrot.getImage(10, 10, 0, 0, 2.0, 0, false));
        logCase("invalid-maxStep", "maxStep=0",
            "maxStep should be greater than zero", badStep.getMessage());
        assertEquals("maxStep should be greater than zero", badStep.getMessage());
    }
}
