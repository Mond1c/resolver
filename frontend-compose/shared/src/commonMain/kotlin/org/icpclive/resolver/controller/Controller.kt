package org.icpclive.resolver.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.createGraph
import org.icpclive.resolver.ServerToControllerMessage
import org.icpclive.resolver.di.AppGraph

@Composable
internal fun Controller() {
    val graph = remember { createGraph<AppGraph>() }
    Controller(graph)
}

@Composable
internal fun Controller(graph: AppGraph, modifier: Modifier = Modifier) {
    val viewModel = viewModel<ControllerViewModel>(factory = graph.controllerViewModelFactory.value)
    val isConnectedWithAuth by viewModel.isConnectedWithAuth.collectAsStateWithLifecycle()

    Row(
        modifier = modifier.fillMaxSize().background(Color.Black),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isConnectedWithAuth) {
            Authentication(viewModel)
        } else {
            Controller(viewModel)
        }
    }
}

@Composable
internal fun Authentication(viewModel: ControllerViewModel) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = login,
            onValueChange = {
                login = it
            },
            label = {
                Text("Login", color = Color.White)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password", color = Color.White)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.authenticate(login, password)
            },
            shape = RectangleShape
        ) {
            Text("Sign in", color = Color.White)
        }
    }
}

@Composable
internal fun Controller(
    viewModel: ControllerViewModel
) {
    val settings by viewModel.settingsFlow.collectAsStateWithLifecycle()
    val stateIndex: MutableState<Int?> = remember { mutableStateOf(null) }
    val selectedIndex: MutableState<Int?> = remember { mutableStateOf(null) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControllerColumn1(viewModel, Modifier.width(IntrinsicSize.Min).weight(1f))
        Spacer(Modifier.width(16.dp))
        ControllerColumn2(viewModel, settings, Modifier.width(IntrinsicSize.Min).weight(1f))
        Spacer(Modifier.width(16.dp))
        ControllerColumn3(viewModel, settings, Modifier.width(IntrinsicSize.Min).weight(1f))
        if (settings?.isGotoEnabled == true) {
            Spacer(Modifier.width(16.dp))
            ControllerColumn4(
                viewModel,
                stateIndex,
                selectedIndex,
                Modifier.width(IntrinsicSize.Min).weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            ControllerColumn5(
                viewModel,
                stateIndex,
                selectedIndex,
                Modifier
                    .padding(end = 16.dp)
                    .width(IntrinsicSize.Min)
                    .weight(2f)
            )
        }
    }
}

@Composable
internal fun ControllerColumn1(
    viewModel: ControllerViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(start = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                viewModel.start()
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Start", color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.stop()
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Stop", color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.changeDirection()
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Change direction", color = Color.White)
        }
    }
}

@Composable
internal fun ControllerColumn2(
    viewModel: ControllerViewModel,
    settings: ServerToControllerMessage.ScoreboardManagerSettings?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                viewModel.up()
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Up", color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.down()
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Down", color = Color.White)
        }
        if (settings != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Current direction: ${settings.direction.name}",
                color = Color.White
            )
        }
    }
}

@Composable
internal fun ControllerColumn3(
    viewModel: ControllerViewModel,
    settings: ServerToControllerMessage.ScoreboardManagerSettings?,
    modifier: Modifier = Modifier
) {
    var factor by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(end = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = factor,
            onValueChange = {
                factor = it
            },
            Modifier.fillMaxWidth(),
            label = {
                Text("Speed factor", color = Color.White)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                factor.toDoubleOrNull()?.let {
                    viewModel.applyFactor(it)
                }
                factor = ""
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Apply speed factor", color = Color.White)
        }
        if (settings != null) {
            Spacer(Modifier.height(8.dp))
            Text("Current speed factor: ${settings.speedFactor}", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Current state: ${viewModel.getStateString(settings.state)}", color = Color.White)
        }
    }
}

@Composable
internal fun ControllerColumn4(
    viewModel: ControllerViewModel,
    stateIndex: MutableState<Int?>,
    selectedIndex: MutableState<Int?>,
    modifier: Modifier = Modifier
) {
    var teamId by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = teamId,
            onValueChange = {
                teamId = it
            },
            Modifier.fillMaxWidth(),
            label = {
                Text("Team id", color = Color.White)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.sendGetVariantsToGotoSignal(teamId)
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Get variants to go to", color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.goto(teamId, stateIndex.value)
                selectedIndex.value = null
                stateIndex.value = null
            },
            Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Go to", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ControllerColumn5(
    viewModel: ControllerViewModel,
    stateIndex: MutableState<Int?>,
    selectedIndex: MutableState<Int?>,
    modifier: Modifier = Modifier
) {
    val variantsToGoto by viewModel.variantsToGotoFlow.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        variantsToGoto?.let {
            if (it.variants.isNotEmpty()) {
                Text(it.fullName, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Column {
                    Button(
                        onClick = {
                            expanded = true
                        },
                        shape = RectangleShape
                    ) {
                        selectedIndex.value?.let { selected ->
                            Text(viewModel.getVariantToGotoString(it.variants[selected]))
                        } ?: Text("Choose variant")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        it.variants.forEachIndexed { index, variant ->
                            DropdownMenuItem(
                                text = { Text(viewModel.getVariantToGotoString(variant)) },
                                onClick = {
                                    selectedIndex.value = index
                                    selectedIndex.value?.let { selected ->
                                        stateIndex.value = it.variants[selected].stateIndex
                                    }
                                    expanded = false
                                },
                                shape = RectangleShape
                            )
                        }
                    }
                }
            }
        }
    }
}