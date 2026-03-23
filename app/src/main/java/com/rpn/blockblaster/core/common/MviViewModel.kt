package com.rpn.blockblaster.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel() {

    private val _state  = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    protected val currentState: S get() = _state.value

    protected fun setState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch { _events.send(event) }
    }

    abstract fun onIntent(intent: I)
}
