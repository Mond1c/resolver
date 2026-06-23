package org.icpclive.resolver.scoreboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.icpclive.resolver.ScoreboardRepository
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@SingleIn(AppScope::class)
@Inject
internal class ScoreboardViewModelFactory(
    private val repository: ScoreboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ScoreboardViewModel(repository) as T
    }
}