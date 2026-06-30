package com.example.rainbow;

import com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A progress bar UI that paints a scrolling rainbow with a marquee label
 * ("BUILDING…") rolling horizontally across it.
 *
 * <p>Inspired by the Nyan Cat progress bar plugin, minus the cat. We extend
 * {@link DarculaProgressBarUI} so we inherit its layout maths and, importantly,
 * its indeterminate {@code Animator}, which drives the repaints that make both
 * the rainbow and the text scroll.
 *
 * <p>This delegate is written in Java (rather than Kotlin) so that the static
 * {@link #createUI} factory can legally hide the one inherited from
 * {@code BasicProgressBarUI}; Kotlin rejects that as an accidental override.
 */
public final class RainbowProgressBarUI extends DarculaProgressBarUI {

    private static final int MIN_HEIGHT = 6;
    private static final long SCROLL_MS = 16L;       // ~ one px step per frame
    private static final float BAND_PERIOD = 140f;   // px for one full rainbow cycle

    private static final long TEXT_SCROLL_MS = 18L;  // ms per px the text travels
    private static final String DEFAULT_TEXT = "BUILDING";
    private static final int TEXT_PADDING = 4;       // extra vertical room for text

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
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size == null) {
            size = new Dimension(146, 0);
        }
        // Make sure the bar is tall enough to fit the marquee text.
        int textHeight = c.getFontMetrics(barFont(c)).getHeight() + TEXT_PADDING;
        size.height = Math.max(size.height, textHeight);
        return size;
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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            java.awt.Insets insets = progressBar.getInsets();
            int barWidth = Math.max(0, c.getWidth() - (insets.left + insets.right));
            int prefH = progressBar.getPreferredSize() != null
                    ? progressBar.getPreferredSize().height : c.getHeight();
            int barHeight = Math.max(MIN_HEIGHT, Math.max(1, Math.min(prefH, c.getHeight())));
            int x = insets.left;
            int y = insets.top + (c.getHeight() - insets.top - insets.bottom - barHeight) / 2;
            float arc = barHeight;

            // Rounded bar outline; everything else is clipped to it.
            RoundRectangle2D.Float barShape =
                    new RoundRectangle2D.Float(x, y, barWidth, barHeight, arc, arc);

            // Track background under the rainbow.
            g2.setColor(trackColor(c));
            g2.fill(barShape);

            int fillWidth = (int) (barWidth * fraction);
            if (fillWidth > 0) {
                // A horizontal rainbow that repeats every BAND_PERIOD px, slid by
                // scrollOffset for the indeterminate animation. Clipped to the
                // rounded fill region so it keeps soft corners.
                Graphics2D rainbow = (Graphics2D) g2.create();
                try {
                    rainbow.clip(new RoundRectangle2D.Float(x, y, fillWidth, barHeight, arc, arc));
                    float gradientStart = x - scrollOffset;
                    float gradientEnd = gradientStart + BAND_PERIOD;
                    rainbow.setPaint(new LinearGradientPaint(
                            gradientStart, 0f, gradientEnd, 0f, FRACTIONS, RAINBOW,
                            MultipleGradientPaint.CycleMethod.REPEAT));
                    rainbow.fillRect(x, y, fillWidth, barHeight);
                } finally {
                    rainbow.dispose();
                }
            }

            // Marquee text rolling horizontally across the whole bar.
            g2.clip(barShape);
            paintScrollingText(g2, c, x, y, barWidth, barHeight);
        } finally {
            g2.dispose();
        }
    }

    private void paintScrollingText(Graphics2D g2, JComponent c,
                                    int x, int y, int barWidth, int barHeight) {
        String text = labelText();
        if (text.isEmpty() || barWidth <= 0) {
            return;
        }

        Font font = barFont(c);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        // The text enters from the right edge, slides left until it fully exits,
        // then loops back — a continuous marquee.
        long loopSpan = barWidth + textWidth;
        long travelled = (System.currentTimeMillis() / TEXT_SCROLL_MS) % loopSpan;
        int textX = (int) (x + barWidth - travelled);
        int baseline = y + (barHeight - fm.getHeight()) / 2 + fm.getAscent();

        // Dark shadow first, then bright text, so it stays legible over any
        // rainbow colour underneath.
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(text, textX + 1, baseline + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, baseline);
    }

    /** The marquee text: the bar's own string if it has one, else "BUILDING". */
    private String labelText() {
        JProgressBar bar = progressBar;
        if (bar != null && bar.isStringPainted()) {
            String s = bar.getString();
            if (s != null && !s.trim().isEmpty()) {
                return s.trim();
            }
        }
        return DEFAULT_TEXT;
    }

    private static Font barFont(JComponent c) {
        Font base = c.getFont();
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        }
        return base.deriveFont(Font.BOLD, Math.max(10f, base.getSize2D() - 1f));
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
