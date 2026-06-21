package com.resolver.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.resolver.ControllerRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@SingleIn(AppScope::class)
@Inject
internal class ControllerViewModelFactory(
    private val repository: ControllerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ControllerViewModel(repository) as T
    }
}