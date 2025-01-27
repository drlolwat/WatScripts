package org.lolwat.misc.mouse;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.algorithm.StandardMouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.utilities.Logger;

import java.awt.*;

public class HumanMouse extends StandardMouseAlgorithm {
    private boolean isMoving = false;

    @Override
    public boolean handleMovement(AbstractMouseDestination abstractMouseDestination) {
        if (isMoving) {
            return false;
        }

        isMoving = true;
        Point suitPos = abstractMouseDestination.getSuitablePoint();
        generatePoints(suitPos);
        isMoving = false;

        return distance(Mouse.getPosition(), suitPos) < 2;
    }

    public void generatePoints(Point point) {
        Point curPos = Mouse.getPosition();
        try {
            // Occasionally add a loop-de-loop
            if (Calculations.random(1, 30) == 1) {
                Point[] loopPoints = generateLoopPoints(curPos, point);
                for (Point loopPoint : loopPoints) {
                    moveCursorWithCubicBezier(curPos, loopPoint, false);
                    curPos = Mouse.getPosition();
                }
            }
        } catch (Exception ignored) { }

        moveCursorWithCubicBezier(curPos, point);
    }

    private Point[] generateLoopPoints(Point start, Point end) {
        int loopSize = 20; // Size of the loop
        Point midPoint = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
        return new Point[]{
                new Point(midPoint.x - loopSize, midPoint.y),
                new Point(midPoint.x, midPoint.y - loopSize),
                new Point(midPoint.x + loopSize, midPoint.y),
                new Point(midPoint.x, midPoint.y + loopSize),
                new Point(midPoint.x - loopSize, midPoint.y)
        };
    }

    private void moveCursorWithCubicBezier(Point startPos, Point endPos, boolean quick) {
        Point controlPoint1 = randomPoint(startPos, endPos);
        Point controlPoint2 = randomPoint(startPos, endPos);
        int steps = quick ? Calculations.random(3, 5) : Calculations.random(5, 15);

        if(distance(startPos, endPos) <= 30) {
            steps = quick ? Calculations.random(3, 5) : Calculations.random(5, 8);
        }

        try {
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Point pointOnCurve = calculateCubicBezierPoint(t, startPos, controlPoint1, controlPoint2, endPos);
                sleep(quick ? Calculations.random(10, 30) : randomSpeed());
                Mouse.hop(pointOnCurve);
            }
        } catch (Exception ignored) { }
    }

    private void moveCursorWithCubicBezier(Point startPos, Point endPos) {
        moveCursorWithCubicBezier(startPos, endPos, false);
    }

    private static Point calculateCubicBezierPoint(double t, Point p0, Point p1, Point p2, Point p3) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double ttt = tt * t;
        double uuu = uu * u;

        Point p = new Point();
        p.x = (int) (uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x);
        p.y = (int) (uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y);

        return p;
    }

    private static Point randomPoint(Point a, Point b) {
        int x = a.x + (int) (Math.random() * (b.x - a.x));
        int y = a.y + (int) (Math.random() * (b.y - a.y));
        return new Point(x, y);
    }

    private static double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private int randomSpeed() {
        return Calculations.random(30, 70);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Logger.log(e);
        }
    }
}