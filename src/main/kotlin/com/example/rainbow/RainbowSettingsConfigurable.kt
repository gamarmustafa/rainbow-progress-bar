package com.example.rainbow

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings page (Settings > Appearance & Behavior > Rainbow Progress Bar)
 * where users edit the marquee phrases, one per line.
 */
class RainbowSettingsConfigurable : Configurable {

    private var textArea: JBTextArea? = null

    override fun getDisplayName(): String = "Rainbow Progress Bar"

    override fun createComponent(): JComponent {
        val area = JBTextArea(15, 50)
        textArea = area

        val restore = JButton("Restore Defaults").apply {
            addActionListener {
                area.text = RainbowSettings.DEFAULT_PHRASES.joinToString("\n")
            }
        }

        return JPanel(BorderLayout(0, JBUI.scale(6))).apply {
            add(JBLabel("Marquee phrases shown on progress bars, one per line:"),
                BorderLayout.NORTH)
            add(JBScrollPane(area), BorderLayout.CENTER)
            add(JPanel(BorderLayout()).apply {
                add(restore, BorderLayout.WEST)
            }, BorderLayout.SOUTH)
        }
    }

    override fun isModified(): Boolean =
        editedPhrases() != RainbowSettings.getInstance().phrases

    override fun apply() {
        RainbowSettings.getInstance().phrases = editedPhrases()
    }

    override fun reset() {
        textArea?.text = RainbowSettings.getInstance().phrases.joinToString("\n")
    }

    override fun disposeUIResources() {
        textArea = null
    }

    private fun editedPhrases(): List<String> =
        (textArea?.text ?: "").lines().map { it.trim() }.filter { it.isNotEmpty() }
}
