package com.example.rainbow;

import com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A progress bar UI that paints a scrolling rainbow instead of the stock fill.
 *
 * <p>Inspired by the Nyan Cat progress bar plugin, minus the cat. We extend
 * {@link DarculaProgressBarUI} so we inherit its layout maths and, importantly,
 * its indeterminate {@code Animator}, which drives the repaints that make the
 * rainbow scroll.
 *
 * <p>This delegate is written in Java (rather than Kotlin) so that the static
 * {@link #createUI} factory can legally hide the one inherited from
 * {@code BasicProgressBarUI}; Kotlin rejects that as an accidental override.
 */
public final class RainbowProgressBarUI extends DarculaProgressBarUI {

    private static final int MIN_HEIGHT = 6;
    private static final long SCROLL_MS = 16L;     // ~ one px step per frame
    private static final float BAND_PERIOD = 140f; // px for one full rainbow cycle

    private static final Color[] RAINBOW = {
            new Color(0xFF595E), // red
            new Color(0xFF924C), // orange
            new Color(0xFFCA3A), // yellow
            new Color(0x8AC926), // green
            new Color(0x1982C4), // blue
            new Color(0x6A4C93), // violet
            new Color(0xFF595E), // back to red for a seamless repeat
    };

    private static final float[] FRACTIONS = {0f, 0.17f, 0.34f, 0.5f, 0.67f, 0.84f, 1f};

    @SuppressWarnings("unused") // invoked reflectively by UIManager
    public static ComponentUI createUI(JComponent c) {
        return new RainbowProgressBarUI();
    }

    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        // Offset derived from wall-clock time so the rainbow scrolls smoothly
        // regardless of the actual repaint cadence.
        int offset = (int) ((System.currentTimeMillis() / SCROLL_MS) % (long) BAND_PERIOD);
        paintRainbow(g, c, 1f, offset);
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        JProgressBar bar = progressBar;
        int span = Math.max(1, bar.getMaximum() - bar.getMinimum());
        float fraction = (float) (bar.getValue() - bar.getMinimum()) / span;
        fraction = Math.max(0f, Math.min(1f, fraction));
        paintRainbow(g, c, fraction, 0);
    }

    private void paintRainbow(Graphics g, JComponent c, float fraction, int scrollOffset) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            java.awt.Insets insets = progressBar.getInsets();
            int barWidth = Math.max(0, c.getWidth() - (insets.left + insets.right));
            int prefH = progressBar.getPreferredSize() != null
                    ? progressBar.getPreferredSize().height : c.getHeight();
            int barHeight = Math.max(MIN_HEIGHT, Math.max(1, Math.min(prefH, c.getHeight())));
            int x = insets.left;
            int y = insets.top + (c.getHeight() - insets.top - insets.bottom - barHeight) / 2;
            float arc = barHeight;

            // Track background (subtle rounded rect under the rainbow).
            RoundRectangle2D.Float track =
                    new RoundRectangle2D.Float(x, y, barWidth, barHeight, arc, arc);
            g2.setColor(trackColor(c));
            g2.fill(track);

            int fillWidth = (int) (barWidth * fraction);
            if (fillWidth <= 0) {
                return;
            }

            // Clip to the rounded fill region so the rainbow keeps soft corners.
            RoundRectangle2D.Float fillShape =
                    new RoundRectangle2D.Float(x, y, fillWidth, barHeight, arc, arc);
            g2.clip(fillShape);

            // A horizontal rainbow that repeats every BAND_PERIOD px, slid by
            // scrollOffset for the indeterminate animation.
            float gradientStart = x - scrollOffset;
            float gradientEnd = gradientStart + BAND_PERIOD;
            g2.setPaint(new LinearGradientPaint(
                    gradientStart, 0f, gradientEnd, 0f, FRACTIONS, RAINBOW,
                    MultipleGradientPaint.CycleMethod.REPEAT));
            g2.fillRect(x, y, fillWidth, barHeight);
        } finally {
            g2.dispose();
        }
    }

    private static Color trackColor(JComponent c) {
        Color bg = c.getParent() != null ? c.getParent().getBackground() : c.getBackground();
        if (bg == null) {
            bg = Color.GRAY;
        }
        boolean dark = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3 < 128;
        return dark ? new Color(255, 255, 255, 28) : new Color(0, 0, 0, 28);
    }
}
