package com.thealgorithms.geometry;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Utility methods for checking and computing 2D line segment intersections.
 *
 * <p>Supported Domain & Boundary Contract:
 * <ul>
 *   <li><b>Coordinate Domain:</b> Endpoints are represented by {@link Point} with 32-bit signed integer
 *       coordinates within [{@link Integer#MIN_VALUE}, {@link Integer#MAX_VALUE}]. Intermediate geometric
 *       determinants use 64-bit {@code long} calculations.</li>
 *   <li><b>Smallest Valid Input (Degenerate Boundary):</b> Zero-length segments where {@code p1.equals(p2)}
 *       and/or {@code q1.equals(q2)} (e.g., origin point {@code (0, 0)}). A degenerate point intersects another
 *       point or segment if and only if it lies on that segment.</li>
 *   <li><b>Interior Crossing:</b> Two non-parallel segments crossing strictly inside their interiors return
 *       {@code intersects = true} and a unique {@link Optional} containing the intersection {@link Point2D.Double}.</li>
 *   <li><b>Touching Endpoint Boundary:</b> Segments sharing a single common vertex return {@code intersects = true}
 *       and {@link Optional} containing that vertex coordinates.</li>
 *   <li><b>T-Junction Boundary:</b> An endpoint of one segment lying on the interior of the other segment returns
 *       {@code intersects = true} and {@link Optional} containing that endpoint.</li>
 *   <li><b>Collinear Overlap Boundary:</b> Collinear segments sharing more than one point return {@code intersects = true}
 *       and {@link Optional#empty()} because no single unique intersection point exists.</li>
 *   <li><b>Parallel / Collinear Disjoint Boundary:</b> Parallel or collinear non-overlapping segments return
 *       {@code intersects = false} and {@link Optional#empty()}.</li>
 *   <li><b>Preconditions:</b> All {@link Point} parameters must be non-null; passing null violates the contract
 *       and results in {@link NullPointerException} (line 23).</li>
 * </ul>
 */
public final class LineIntersection {
    private LineIntersection() {
    }

    /**
     * Checks whether two line segments intersect.
     *
     * @param p1 first endpoint of segment 1
     * @param p2 second endpoint of segment 1
     * @param q1 first endpoint of segment 2
     * @param q2 second endpoint of segment 2
     * @return true when the segments intersect (including touching endpoints)
     */
    public static boolean intersects(Point p1, Point p2, Point q1, Point q2) {
        int o1 = orientation(p1, p2, q1);
        int o2 = orientation(p1, p2, q2);
        int o3 = orientation(q1, q2, p1);
        int o4 = orientation(q1, q2, p2);

        if (o1 != o2 && o3 != o4) {
            return true;
        }

        if (o1 == 0 && onSegment(p1, q1, p2)) {
            return true;
        }
        if (o2 == 0 && onSegment(p1, q2, p2)) {
            return true;
        }
        if (o3 == 0 && onSegment(q1, p1, q2)) {
            return true;
        }
        if (o4 == 0 && onSegment(q1, p2, q2)) {
            return true;
        }

        return false;
    }

    /**
     * Computes the single geometric intersection point between two non-parallel
     * segments when it exists.
     *
     * <p>For parallel/collinear overlap, this method returns {@code Optional.empty()}.
     *
     * @param p1 first endpoint of segment 1
     * @param p2 second endpoint of segment 1
     * @param q1 first endpoint of segment 2
     * @param q2 second endpoint of segment 2
     * @return the intersection point when uniquely defined and on both segments
     */
    public static Optional<Point2D.Double> intersectionPoint(Point p1, Point p2, Point q1, Point q2) {
        if (!intersects(p1, p2, q1, q2)) {
            return Optional.empty();
        }

        long x1 = p1.x();
        long y1 = p1.y();
        long x2 = p2.x();
        long y2 = p2.y();
        long x3 = q1.x();
        long y3 = q1.y();
        long x4 = q2.x();
        long y4 = q2.y();

        long denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denominator == 0L) {
            return sharedEndpoint(p1, p2, q1, q2);
        }

        long determinant1 = x1 * y2 - y1 * x2;
        long determinant2 = x3 * y4 - y3 * x4;
        long numeratorX = determinant1 * (x3 - x4) - (x1 - x2) * determinant2;
        long numeratorY = determinant1 * (y3 - y4) - (y1 - y2) * determinant2;

        return Optional.of(new Point2D.Double(numeratorX / (double) denominator, numeratorY / (double) denominator));
    }

    private static int orientation(Point a, Point b, Point c) {
        long cross = ((long) b.x() - a.x()) * ((long) c.y() - a.y()) - ((long) b.y() - a.y()) * ((long) c.x() - a.x());
        return Long.compare(cross, 0L);
    }

    private static Optional<Point2D.Double> sharedEndpoint(Point p1, Point p2, Point q1, Point q2) {
        if (p1.equals(q1) || p1.equals(q2)) {
            return Optional.of(new Point2D.Double(p1.x(), p1.y()));
        }
        if (p2.equals(q1) || p2.equals(q2)) {
            return Optional.of(new Point2D.Double(p2.x(), p2.y()));
        }
        return Optional.empty();
    }

    private static boolean onSegment(Point a, Point b, Point c) {
        return b.x() >= Math.min(a.x(), c.x()) && b.x() <= Math.max(a.x(), c.x()) && b.y() >= Math.min(a.y(), c.y()) && b.y() <= Math.max(a.y(), c.y());
    }
}
