package com.irv205.apimockinterceptor.ui.networklog

import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.network.NetworkCall
import com.irv205.apimockinterceptor.data.network.NetworkCallStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NetworkLogViewModel @Inject constructor(
    private val store: NetworkCallStore,
) : ViewModel() {

    val calls: StateFlow<List<NetworkCall>> = store.calls

    fun clear() = store.clear()
}
