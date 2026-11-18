package com.thealgorithms.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * A Java program that computes the convex hull using the Graham Scan algorithm.
 *
 * <p>Complexity (where {@code n = points.length} is the number of input points):
 * <ul>
 *   <li>Theoretical worst-case time: {@code O(n log n)}, dominated by the two
 *       {@link Arrays#sort} calls (coordinate sort and polar-angle sort); the subsequent
 *       stack scan pushes and pops each point at most once in {@code O(n)} time.</li>
 *   <li>Theoretical best-case time: {@code O(n)} when TimSort encounters already-ordered
 *       input in both sorting passes.</li>
 *   <li>Empirical scaling on random 2D integer point clouds grows slightly faster than linear
 *       with {@code n}, consistent with {@code O(n log n)}.</li>
 *   <li>Auxiliary space complexity: {@code O(n)} for the hull stack and sort buffers.</li>
 * </ul>
 *
 * <p>Preconditions: {@code points} must be non-null with {@code points.length >= 1} and non-null
 * integral-coordinate {@link Point} elements small enough to avoid 32-bit cross-product overflow
 * in {@link Point#orientation(Point, Point, Point)}. The constructor sorts {@code points} in place.
 *
 * References:
 * https://github.com/TheAlgorithms/C-Plus-Plus/blob/master/geometry/graham_scan_algorithm.cpp
 * https://github.com/TheAlgorithms/C-Plus-Plus/blob/master/geometry/graham_scan_functions.hpp
 * https://algs4.cs.princeton.edu/99hull/GrahamScan.java.html
 */
public class GrahamScan {

    private final Stack<Point> hull = new Stack<>();

    public GrahamScan(Point[] points) {
        // Pre-process points: sort by y-coordinate, then by polar order with respect to the first point
        Arrays.sort(points);
        Arrays.sort(points, 1, points.length, points[0].polarOrder());

        hull.push(points[0]);

        // Find the first point not equal to points[0] (firstNonEqualIndex)
        // and the first point not collinear firstNonCollinearIndex with the previous points
        int firstNonEqualIndex;
        for (firstNonEqualIndex = 1; firstNonEqualIndex < points.length; firstNonEqualIndex++) {
            if (!points[0].equals(points[firstNonEqualIndex])) {
                break;
            }
        }

        if (firstNonEqualIndex == points.length) {
            return;
        }

        int firstNonCollinearIndex;
        for (firstNonCollinearIndex = firstNonEqualIndex + 1; firstNonCollinearIndex < points.length; firstNonCollinearIndex++) {
            if (Point.orientation(points[0], points[firstNonEqualIndex], points[firstNonCollinearIndex]) != 0) {
                break;
            }
        }

        hull.push(points[firstNonCollinearIndex - 1]);

        // Process the remaining points and update the hull
        for (int i = firstNonCollinearIndex; i < points.length; i++) {
            Point top = hull.pop();
            while (Point.orientation(hull.peek(), top, points[i]) <= 0) {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points[i]);
        }
    }

    /**
     * @return An iterable collection of points representing the convex hull.
     */
    public Iterable<Point> hull() {
        return new ArrayList<>(hull);
    }
}
