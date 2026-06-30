package com.example.rainbow

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener

/**
 * Wires the rainbow UI into the IDE lifecycle:
 *  - on app start, install it;
 *  - on every Look-and-Feel change, re-install it (a LaF switch resets the
 *    progress-bar UI back to the platform default).
 */
class RainbowProgressBarActivator : AppLifecycleListener, LafManagerListener {

    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        RainbowProgressBars.install()
    }

    override fun lookAndFeelChanged(source: LafManager) {
        RainbowProgressBars.reinstall()
    }
}
