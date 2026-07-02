package com.example.rainbow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Application-level persistent settings: the list of marquee phrases the
 * progress bar cycles through. Stored in rainbowProgressBar.xml under the
 * IDE's per-user config directory.
 */
@Service(Service.Level.APP)
@State(name = "RainbowProgressBarSettings", storages = [Storage("rainbowProgressBar.xml")])
class RainbowSettings : PersistentStateComponent<RainbowSettings.State> {

    class State {
        var phrases: MutableList<String> = DEFAULT_PHRASES.toMutableList()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var phrases: List<String>
        get() = state.phrases
        set(value) {
            state.phrases = value.map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        }

    /**
     * Phrases for the marquee to paint. Never empty: if the user removed every
     * phrase, fall back to the defaults so the bar isn't blank.
     */
    fun effectivePhrases(): Array<String> =
        (state.phrases.takeIf { it.isNotEmpty() } ?: DEFAULT_PHRASES).toTypedArray()

    companion object {
        @JvmStatic
        fun getInstance(): RainbowSettings =
            ApplicationManager.getApplication().getService(RainbowSettings::class.java)

        val DEFAULT_PHRASES: List<String> = listOf(
            "COMPILING... PROBABLY",
            "SUMMONING BYTES",
            "IT WORKS ON MY MACHINE",
            "TRUST THE PROCESS",
            "ALMOST THERE (TOTALLY LYING)",
            "DON'T PANIC",
            "BRB OPTIMIZING VIBES",
            "MAKING IT WORK... ISH",
            "HEROICALLY DOING NOTHING",
            "TURNING IT OFF AND ON AGAIN",
            "DOWNLOADING MORE RAM",
            "99 LITTLE BUGS IN THE CODE...",
            "CONSULTING THE RUBBER DUCK",
            "PLEASE WAIT... OR DON'T",
            "REBUILDING... FOR SOME REASON",
        )
    }
}
