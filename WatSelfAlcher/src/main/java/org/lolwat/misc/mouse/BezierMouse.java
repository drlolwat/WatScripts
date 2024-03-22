package org.lolwat.misc.mouse;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.mouse.MouseButton;
import org.dreambot.api.input.mouse.algorithm.MouseAlgorithm;
import org.dreambot.api.input.mouse.algorithm.StandardMouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.Calculations;
import org.lolwat.WatAIO;
import org.lolwat.managers.ConfigManager;

import java.awt.*;

public class BezierMouse implements MouseAlgorithm {
    private boolean isMoving = false;
    private static boolean INSTANT_HOP = false;
    private StandardMouseAlgorithm basic = new StandardMouseAlgorithm();

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

    @Override
    public boolean handleClick(MouseButton mouseButton) {
        return basic.handleClick(mouseButton);
    }

    public void generatePoints(Point point) {
        Point curPos = Mouse.getPosition();
        double initialDistance = distance(point, curPos);
        double currentDistance = initialDistance;

        try {
            while (currentDistance / initialDistance > 0.05 && Calculations.random(1) == 2) {
                Point rp = randomPoint(curPos, point);
                moveCursorWithCubicBezier(curPos, rp);
                curPos = Mouse.getPosition();
                currentDistance = distance(point, curPos);
            }
        } catch (Exception ignored) { }
        moveCursorWithCubicBezier(curPos, point);
    }

    private void moveCursorWithCubicBezier(Point startPos, Point endPos) {
        if (INSTANT_HOP) {
            Mouse.hop(endPos);
            sleep(35);
            return;
        }

        Point controlPoint1 = randomPoint(startPos, endPos);
        Point controlPoint2 = randomPoint(startPos, endPos);
        int steps = Calculations.random(5, 15);

        if(distance(startPos, endPos) <= 30) {
            steps = Calculations.random(5, 8);
        }

        try {
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Point pointOnCurve = calculateCubicBezierPoint(t, startPos, controlPoint1, controlPoint2, endPos);
                sleep(randomSpeed());
                Mouse.hop(pointOnCurve);
            }
        } catch (Exception ignored) { }
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
            e.printStackTrace();
        }
    }
}