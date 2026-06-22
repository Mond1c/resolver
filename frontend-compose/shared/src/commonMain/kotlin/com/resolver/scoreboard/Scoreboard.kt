package com.resolver.scoreboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.resolver.ICPCProblemResult
import com.resolver.IOIProblemResult
import com.resolver.ProblemId
import com.resolver.ProblemInfo
import com.resolver.ScoreboardRow
import com.resolver.di.AppGraph
import dev.zacsweers.metro.createGraph
import kotlin.time.Duration

@Composable
internal fun Scoreboard() {
    val graph = remember { createGraph<AppGraph>() }
    Scoreboard(graph)
}

@Composable
internal fun Scoreboard(graph: AppGraph, modifier: Modifier = Modifier) {
    val viewModel = viewModel<ScoreboardViewModel>(factory = graph.scoreboardViewModelFactory.value)

    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    if (isConnected) {
        Connected(viewModel, modifier)
    } else {
        NotConnected(modifier)
    }
}

@Composable
internal fun NotConnected(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Connection hasn't been established yet", color = Color.White)
    }
}

@Composable
internal fun Connected(
    viewModel: ScoreboardViewModel,
    modifier: Modifier = Modifier
) {
    val problems by viewModel.problems.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize().background(Color.Black)) {
        Header(problems)
        Scoreboard(viewModel, problems)
    }
}

@Composable
internal fun Scoreboard(
    viewModel: ScoreboardViewModel,
    problems: List<ProblemInfo>
) {
    val scoreboard by viewModel.scoreboard.collectAsStateWithLifecycle()
    val chosenRowTeamId by viewModel.chosenRowTeamId.collectAsStateWithLifecycle()
    val chosenRowIndex by viewModel.chosenRowIndex.collectAsState()
    val chosenProblemId by viewModel.chosenProblem.collectAsStateWithLifecycle()
    val isICPC by viewModel.isICPC.collectAsStateWithLifecycle()

    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val listState = rememberLazyListState()

    LaunchedEffect(scoreboard.size) {
        if (scoreboard.isNotEmpty()) {
            listState.scrollToItem(scoreboard.size - 1)
        }
    }

    LaunchedEffect(chosenRowIndex) {
        chosenRowIndex?.let {
            listState.scrollToItem(it, -300)
        }
    }

    LazyColumn(Modifier.fillMaxSize(), state = listState, userScrollEnabled = false) {
        items(scoreboard, key = { it.teamId }) { item ->
            val isChosen = chosenRowTeamId == item.teamId

            Column(
                modifier = Modifier
                    .animateItem(
                        placementSpec = tween(
                            durationMillis = if (isChosen) 1000 else 1500,
                            easing = LinearOutSlowInEasing
                        ),
                        fadeInSpec = null,
                        fadeOutSpec = null
                    )
                    .fillMaxWidth()
                    .background(Color.Black)
                    .zIndex(if (isChosen) 100f else 1f)
                    .then(
                        if (isChosen)
                            Modifier.background(Color(1, 119, 231, 255))
                        else Modifier
                    )
                    .border(1.dp, Color.White)
            ) {
                Spacer(Modifier.height(8.dp))
                TeamName(item.name)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Rank(item.rank)
                    if (isICPC) {
                        ICPCProblems(
                            row = item.row,
                            problems = problems,
                            chosenProblemId = chosenProblemId,
                            scale = scale,
                            isChosen = isChosen
                        )
                    } else {
                        IOIProblems(
                            row = item.row,
                            problems = problems,
                            chosenProblemId = chosenProblemId,
                            scale = scale,
                            isChosen = isChosen
                        )
                    }
                    Penalty(item.row.penalty)
                    TotalScore(item.row.totalScore)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun TeamName(
    name: String
) {
    Text(
        name,
        Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = Color.White
    )
}

@Composable
internal fun RowScope.TotalScore(
    totalScore: Double
) {
    Text(
        totalScore.toInt().toString(),
        Modifier.weight(1f),
        textAlign = TextAlign.Center,
        color = Color.White
    )
}

@Composable
internal fun RowScope.Penalty(
    penalty: Duration
) {
    Text(
        penalty.inWholeMinutes.toString(),
        Modifier.weight(1f),
        textAlign = TextAlign.Center,
        color = Color.White
    )
}

@Composable
internal fun RowScope.Rank(
    rank: Int
) {
    Text(
        rank.toString(),
        Modifier.weight(1f),
        textAlign = TextAlign.Center,
        color = Color.White
    )
}

@Composable
internal fun RowScope.ICPCProblems(
    row: ScoreboardRow,
    problems: List<ProblemInfo>,
    chosenProblemId: ProblemId?,
    scale: Float,
    isChosen: Boolean
) {
    row.problemResults.forEachIndexed { index, result ->
        result as ICPCProblemResult
        if (!isChosen || chosenProblemId != problems[index].id) {
            ICPCCell(result)
        } else {
            ICPCChosenCell(result, scale)
        }
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
internal fun RowScope.IOIProblems(
    row: ScoreboardRow,
    problems: List<ProblemInfo>,
    chosenProblemId: ProblemId?,
    scale: Float,
    isChosen: Boolean
) {
    row.problemResults.forEachIndexed { index, result ->
        result as IOIProblemResult
        if (!isChosen || chosenProblemId != problems[index].id) {
            IOICell(result)
        } else {
            IOIChosenCell(result, scale)
        }
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
internal fun Header(problems: List<ProblemInfo>) {
    Row {
        Text("Rank", Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.White)
        problems.forEach {
            Text(
                it.displayName,
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
        Text("Time", Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.White)
        Text("Solved", Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.White)
    }
}

internal fun chooseICPCColour(result: ICPCProblemResult): Color {
    return if (result.pendingAttempts > 0) {
        Color.Yellow
    } else if (result.isSolved) {
        Color.Green
    } else if (result.wrongAttempts > 0) {
        Color.Red
    } else {
        Color.Black
    }
}

internal fun chooseIOIColour(result: IOIProblemResult): Color {
    return if (result.pendingAttempts > 0) {
        Color.Yellow
    } else if (result.score?.let { it > 0 }?.takeIf { it } != null) {
        Color.Green
    } else if (result.totalAttempts > 0) {
        Color.Red
    } else {
        Color.Black
    }
}

internal fun getICPCVisual(result: ICPCProblemResult): String {
    return if (result.isSolved) {
        if (result.wrongAttempts == 0) {
            "+"
        } else {
            "+${result.wrongAttempts}"
        }
    } else if (result.pendingAttempts == 0) {
        if (result.wrongAttempts != 0) {
            "-${result.wrongAttempts}"
        } else {
            ""
        }
    } else {
        "?${result.pendingAttempts + result.wrongAttempts}"
    }
}

internal fun getIOIVisual(result: IOIProblemResult): String {
    return if (result.pendingAttempts > 0) {
        "${result.score ?: 0}?"
    } else if (result.totalAttempts > 0 && result.score != null) {
        result.score.toString()
    } else {
        ""
    }
}

@Composable
internal fun RowScope.ICPCCell(result: ICPCProblemResult) {
    val colour = chooseICPCColour(result)
    Text(
        getICPCVisual(result),
        if (colour != Color.Black) {
            Modifier.weight(1f).background(colour)
        } else {
            Modifier.weight(1f)
        },
        textAlign = TextAlign.Center
    )
}

@Composable
internal fun RowScope.ICPCChosenCell(result: ICPCProblemResult, scale: Float) {
    val colour = chooseICPCColour(result)
    Text(
        getICPCVisual(result),
        if (colour != Color.Green && colour != Color.Red) {
            Modifier.weight(1f).background(colour).scale(scale)
        } else {
            Modifier.weight(1f).background(colour)
        },
        textAlign = TextAlign.Center
    )
}

@Composable
internal fun RowScope.IOICell(result: IOIProblemResult) {
    val colour = chooseIOIColour(result)
    Text(
        getIOIVisual(result),
        if (colour != Color.Black) {
            Modifier.weight(1f).background(colour)
        } else {
            Modifier.weight(1f)
        },
        textAlign = TextAlign.Center
    )
}

@Composable
internal fun RowScope.IOIChosenCell(result: IOIProblemResult, scale: Float) {
    val colour = chooseIOIColour(result)
    Text(
        getIOIVisual(result),
        if (colour != Color.Green && colour != Color.Red) {
            Modifier.weight(1f).background(colour).scale(scale)
        } else {
            Modifier.weight(1f).background(colour)
        },
        textAlign = TextAlign.Center
    )
}