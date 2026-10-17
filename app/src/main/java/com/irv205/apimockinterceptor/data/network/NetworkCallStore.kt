package com.irv205.apimockinterceptor.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkCallStore @Inject constructor() {

    private val idCounter = AtomicLong(0L)

    private val _calls = MutableStateFlow<List<NetworkCall>>(emptyList())
    val calls: StateFlow<List<NetworkCall>> = _calls.asStateFlow()

    fun add(call: NetworkCall) {
        val stored = call.copy(id = idCounter.incrementAndGet())
        _calls.update { current -> listOf(stored) + current }
    }

    fun clear() {
        _calls.value = emptyList()
    }
}
