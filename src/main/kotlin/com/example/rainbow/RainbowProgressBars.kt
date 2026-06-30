package com.example.rainbow

import com.intellij.openapi.diagnostic.logger
import java.awt.Window
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Installs [RainbowProgressBarUI] as the UI delegate for every [JProgressBar].
 *
 * Swing decides a progress bar's UI by looking up the class name stored in the
 * UI defaults under the key "ProgressBarUI" (the component's UIClassID). We
 * point that at our class. Because our class lives in a plugin classloader that
 * the platform's default-loader can't see, we also stash the actual [Class]
 * object in the defaults so [UIManager.getUI] resolves it directly.
 */
object RainbowProgressBars {
    private val LOG = logger<RainbowProgressBars>()
    private val uiClassName = RainbowProgressBarUI::class.java.name

    fun install() {
        runOnEdt {
            val defaults = UIManager.getDefaults()
            // Pre-resolve the class so UIManager doesn't try (and fail) to load
            // it through the platform classloader.
            defaults[uiClassName] = RainbowProgressBarUI::class.java
            defaults["ProgressBarUI"] = uiClassName
            refreshExistingBars()
            LOG.info("Rainbow progress bar UI installed")
        }
    }

    /** Re-apply after a LaF change, which resets ProgressBarUI to the default. */
    fun reinstall() = install()

    private fun refreshExistingBars() {
        for (window in Window.getWindows()) {
            updateBarsIn(window)
        }
    }

    private fun updateBarsIn(component: java.awt.Component) {
        if (component is JProgressBar) {
            component.updateUI()
        }
        if (component is java.awt.Container) {
            for (child in component.components) {
                updateBarsIn(child)
            }
        }
    }

    private fun runOnEdt(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) block() else SwingUtilities.invokeLater(block)
    }
}
