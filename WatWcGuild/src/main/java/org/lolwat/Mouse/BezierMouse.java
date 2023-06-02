package org.lolwat.Mouse;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.algorithm.MouseMovementAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;

public class BezierMouse implements MouseMovementAlgorithm {
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
        double initialDistance = distance(point, curPos);
        double currentDistance = initialDistance;

        try {
            while (currentDistance / initialDistance > 0.05 && Calculations.random(1) == 2) { // Loop until distance ratio is below 5% or counter reaches 10
                Point rp = randomPoint(curPos, point);
                moveCursor(curPos, rp);
                curPos = Mouse.getPosition();
                currentDistance = distance(point, curPos);
            }
        } catch (Exception ignored) { }
        moveCursor(curPos, point);
    }

    private void moveCursor(Point startPos, Point endPos) {
        Point controlPoint = randomPoint(startPos, endPos);
        int steps = Calculations.random(5, 20);

        if(distance(startPos, endPos) <= 30) {
            steps = Calculations.random(6, 12);
        }

        try {
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Point pointOnCurve = calculateBezierPoint(t, startPos, controlPoint, endPos);
                Thread.sleep(34);
                Mouse.hop(pointOnCurve);
            }
        } catch (Exception ignored) { }
    }

    private static Point calculateBezierPoint(double t, Point p0, Point p1, Point p2) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;

        Point p = new Point();
        p.x = (int) (uu * p0.x + 2 * u * t * p1.x + tt * p2.x);
        p.y = (int) (uu * p0.y + 2 * u * t * p1.y + tt * p2.y);

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
}