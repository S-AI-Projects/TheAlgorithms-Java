package com.thealgorithms.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LineIntersectionTest {

    @Test
    void testCrossingSegments() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(4, 4);
        Point q1 = new Point(0, 4);
        Point q2 = new Point(4, 0);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(2.0, intersection.orElseThrow().getY(), 1e-9);
    }

    @Test
    void testParallelSegments() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(3, 3);
        Point q1 = new Point(0, 1);
        Point q2 = new Point(3, 4);

        assertFalse(LineIntersection.intersects(p1, p2, q1, q2));
        assertTrue(LineIntersection.intersectionPoint(p1, p2, q1, q2).isEmpty());
    }

    @Test
    void testTouchingAtEndpoint() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(2, 2);
        Point q1 = new Point(2, 2);
        Point q2 = new Point(4, 0);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(2.0, intersection.orElseThrow().getY(), 1e-9);
    }

    @Test
    void testCollinearOverlapHasNoUniquePoint() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(4, 4);
        Point q1 = new Point(2, 2);
        Point q2 = new Point(6, 6);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        assertTrue(LineIntersection.intersectionPoint(p1, p2, q1, q2).isEmpty());
    }

    @Test
    void testCollinearDisjointSegments() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(2, 2);
        Point q1 = new Point(3, 3);
        Point q2 = new Point(5, 5);

        assertFalse(LineIntersection.intersects(p1, p2, q1, q2));
        assertTrue(LineIntersection.intersectionPoint(p1, p2, q1, q2).isEmpty());
    }

    @Test
    void testCollinearSegmentsTouchingAtEndpointHaveUniquePoint() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(2, 2);
        Point q1 = new Point(2, 2);
        Point q2 = new Point(4, 4);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(2.0, intersection.orElseThrow().getY(), 1e-9);
    }

    @Test
    void testVerticalAndHorizontalCrossingSegments() {
        Point p1 = new Point(2, 0);
        Point p2 = new Point(2, 5);
        Point q1 = new Point(0, 3);
        Point q2 = new Point(4, 3);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(3.0, intersection.orElseThrow().getY(), 1e-9);
    }

    /**
     * Smallest valid input fixture: zero-length (degenerate) segment at the origin.
     * Preconditions established at:
     * - LineIntersection.java line 22 (public static boolean intersects)
     * - LineIntersection.java line 32 (o1 == 0 && onSegment(p1, q1, p2))
     * - LineIntersection.java line 75-76 (denominator == 0L -> sharedEndpoint)
     */
    @Test
    void testSmallestValidInputDegenerateIdenticalPoint() {
        // Literal inputs: single point (0, 0) intersecting itself
        Point p1 = new Point(0, 0);
        Point p2 = new Point(0, 0);
        Point q1 = new Point(0, 0);
        Point q2 = new Point(0, 0);

        // Inside boundary: identical degenerate points intersect at (0.0, 0.0)
        assertTrue(LineIntersection.intersects(p1, p2, q1, q2), "Identical degenerate zero-length segments must intersect.");
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent(), "Intersection point must be present for identical degenerate segments.");
        assertEquals(0.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(0.0, intersection.orElseThrow().getY(), 1e-9);

        // Outside boundary: disjoint degenerate points (0, 0) and (1, 1) do not intersect
        Point r1 = new Point(1, 1);
        Point r2 = new Point(1, 1);
        assertFalse(LineIntersection.intersects(p1, p2, r1, r2), "Disjoint degenerate points must not intersect.");
        assertTrue(LineIntersection.intersectionPoint(p1, p2, r1, r2).isEmpty(), "Disjoint degenerate points must return Optional.empty().");
    }

    /**
     * Ordinary input fixture: standard interior crossing.
     * Preconditions established at:
     * - LineIntersection.java line 28 (o1 != o2 && o3 != o4)
     * - LineIntersection.java line 74 (denominator calculation)
     * - LineIntersection.java line 84 (unique Point2D.Double return)
     */
    @Test
    void testOrdinaryInputStandardCrossing() {
        // Literal inputs: (1, 1)-(5, 5) and (1, 5)-(5, 1)
        Point p1 = new Point(1, 1);
        Point p2 = new Point(5, 5);
        Point q1 = new Point(1, 5);
        Point q2 = new Point(5, 1);

        // Independently calculated expected intersection: (3.0, 3.0)
        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(3.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(3.0, intersection.orElseThrow().getY(), 1e-9);
    }

    /**
     * Boundary fixture: T-junction where an endpoint lies on the interior of the other segment.
     * Preconditions established at:
     * - LineIntersection.java line 38-39 (o3 == 0 && onSegment(q1, p1, q2))
     * - LineIntersection.java line 84 (unique intersection point)
     */
    @Test
    void testBoundaryTJunctionEndpointOnSegment() {
        // Inside boundary: q1(2, 2) lies directly on segment p1(0, 2)-p2(4, 2)
        Point p1 = new Point(0, 2);
        Point p2 = new Point(4, 2);
        Point q1 = new Point(2, 2);
        Point q2 = new Point(2, 6);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(2.0, intersection.orElseThrow().getY(), 1e-9);

        // Outside boundary: move q1 by 1 unit away to (2, 3) - no intersection
        Point q1Separated = new Point(2, 3);
        assertFalse(LineIntersection.intersects(p1, p2, q1Separated, q2));
        assertTrue(LineIntersection.intersectionPoint(p1, p2, q1Separated, q2).isEmpty());
    }

    /**
     * Boundary fixture: touching at endpoint non-collinearly.
     * Preconditions established at:
     * - LineIntersection.java line 32-37 (orientation == 0 && onSegment)
     * - LineIntersection.java line 84
     */
    @Test
    void testBoundaryTouchingEndpointNonCollinear() {
        // Inside boundary: touch at vertex (2, 2)
        Point p1 = new Point(0, 0);
        Point p2 = new Point(2, 2);
        Point q1 = new Point(2, 2);
        Point q2 = new Point(4, 0);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(2.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(2.0, intersection.orElseThrow().getY(), 1e-9);

        // Outside boundary: shifted by 1 unit gap
        Point q1Shifted = new Point(2, 3);
        Point q2Shifted = new Point(4, 1);
        assertFalse(LineIntersection.intersects(p1, p2, q1Shifted, q2Shifted));
        assertTrue(LineIntersection.intersectionPoint(p1, p2, q1Shifted, q2Shifted).isEmpty());
    }

    /**
     * Boundary fixture: extreme coordinate domain boundaries.
     * Preconditions established at:
     * - LineIntersection.java line 88 (64-bit cross product prevention of overflow)
     */
    @Test
    void testBoundaryExtremeCoordinateDomain() {
        Point p1 = new Point(-1000000, 0);
        Point p2 = new Point(1000000, 0);
        Point q1 = new Point(0, -1000000);
        Point q2 = new Point(0, 1000000);

        assertTrue(LineIntersection.intersects(p1, p2, q1, q2));
        Optional<Point2D.Double> intersection = LineIntersection.intersectionPoint(p1, p2, q1, q2);
        assertTrue(intersection.isPresent());
        assertEquals(0.0, intersection.orElseThrow().getX(), 1e-9);
        assertEquals(0.0, intersection.orElseThrow().getY(), 1e-9);
    }

    /**
     * Boundary fixture: null precondition violation.
     * Preconditions established at:
     * - LineIntersection.java line 23 (accessing p1.x() throws NullPointerException)
     */
    @Test
    void testBoundaryNullInputThrowsNullPointerException() {
        Point p = new Point(0, 0);
        assertThrows(NullPointerException.class, () -> LineIntersection.intersects(null, p, p, p));
        assertThrows(NullPointerException.class, () -> LineIntersection.intersectionPoint(null, p, p, p));
    }

    /**
     * Reflection fixture for private constructor contract.
     * Preconditions established at:
     * - LineIntersection.java line 10-11
     */
    @Test
    void testPrivateConstructorContract() throws Exception {
        Constructor<LineIntersection> constructor = LineIntersection.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        LineIntersection instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
