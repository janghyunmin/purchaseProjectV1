package run.piece.dev.refactoring.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class FragmentLifecycleOwner : LifecycleOwner {
    override val lifecycle = LifecycleRegistry(this)

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycle.handleLifecycleEvent(event)
    }
}
