package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiGeometryTest {

    @Test
    void fractionOfScreenRespectsMinimum() {
        Dimension d = UiGeometry.fractionOfScreen(0.3, 0.3);
        assertTrue(d.width >= 320);
        assertTrue(d.height >= 240);
    }

    @Test
    void centerLocationWithinBounds() {
        Rectangle bounds = new Rectangle(100, 50, 800, 600);
        Dimension window = new Dimension(200, 100);
        Point p = UiGeometry.centerLocation(bounds, window);
        assertEquals(400, p.x);
        assertEquals(300, p.y);
    }

    @Test
    void centerLocationWithOffsetMonitor() {
        Rectangle bounds = new Rectangle(1920, 0, 1920, 1080);
        Dimension window = new Dimension(400, 300);
        Point p = UiGeometry.centerLocation(bounds, window);
        assertEquals(1920 + (1920 - 400) / 2, p.x);
        assertEquals(0 + (1080 - 300) / 2, p.y);
    }

    @Test
    void effectiveSizeUsesPreferredWhenSizeZero() {
        Dimension size = new Dimension(0, 0);
        Dimension preferred = new Dimension(480, 200);
        Dimension effective = UiGeometry.effectiveSize(size, preferred);
        assertEquals(480, effective.width);
        assertEquals(200, effective.height);
    }
}
