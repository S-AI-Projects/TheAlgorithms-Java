package com.thealgorithms.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GrahamScanTest {
    @Test
    void testGrahamScan() {
        Point[] points = {new Point(0, 3), new Point(1, 1), new Point(2, 2), new Point(4, 4), new Point(0, 0), new Point(1, 2), new Point(3, 1), new Point(3, 3)};
        String expectedResult = "[(0, 0), (3, 1), (4, 4), (0, 3)]";

        GrahamScan graham = new GrahamScan(points);
        assertEquals(expectedResult, graham.hull().toString());
    }

    @Test
    @DisplayName("Fixture: smallest single-point input and ordinary triangle with interior point")
    void testSmallestAndOrdinaryFixtures() {
        // Preconditions from GrahamScan.java (pinned lines):
        // lines 24-25 (points != null, points.length >= 1, sorted in place),
        // line 27 & lines 38-40 (single point or all identical points return 1-point hull),
        // lines 52-59 (CCW hull scan).
        Point[] single = {new Point(0, 0)};
        GrahamScan singleScan = new GrahamScan(single);
        String actualSingle = singleScan.hull().toString();
        assertEquals("[(0, 0)]", actualSingle);

        Point[] ordinary = {new Point(2, 4), new Point(2, 1), new Point(4, 0), new Point(0, 0)};
        GrahamScan ordinaryScan = new GrahamScan(ordinary);
        String actualOrdinary = ordinaryScan.hull().toString();
        assertEquals("[(0, 0), (4, 0), (2, 4)]", actualOrdinary);

        System.out.println("CASE|smallest_single_point|n=1,points=[(0,0)]|expected=[(0, 0)]|actual=" + actualSingle);
        System.out.println("CASE|ordinary_triangle_with_interior|n=4,points=[(2,4),(2,1),(4,0),(0,0)]|expected=[(0, 0), (4, 0), (2, 4)]|actual=" + actualOrdinary);
    }
}
