package com.irv205.apimockinterceptor.ui.mock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.irv205.apimockinterceptor.data.mock.MockScenario
import com.irv205.apimockinterceptor.data.mock.MockService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockScenariosScreen(
    onBack: () -> Unit,
    onEditService: (String) -> Unit,
    onCreateScenario: (String) -> Unit,
    onEditScenario: (String, String) -> Unit,
    viewModel: MockScenariosViewModel = hiltViewModel(),
) {
    val service by viewModel.service.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<MockScenario?>(null) }

    val current = service
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = current?.name ?: "Escenarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    if (current != null) {
                        IconButton(onClick = { onEditService(current.id) }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar servicio")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (current != null) {
                FloatingActionButton(onClick = { onCreateScenario(current.id) }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Nuevo escenario")
                }
            }
        },
    ) { innerPadding ->
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Text(
                    text = "Servicio no encontrado",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${current.method} ${current.pathPattern}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = if (current.enabled) "Servicio activo" else "Servicio desactivado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = current.enabled,
                    onCheckedChange = { viewModel.setEnabled(it) },
                )
            }

            if (current.scenarios.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Sin escenarios. Agrega uno con el botón +.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = current.scenarios, key = { it.id }) { scenario ->
                        ScenarioCard(
                            scenario = scenario,
                            selected = scenario.id == current.selectedScenarioId,
                            onClick = { viewModel.select(scenario.id) },
                            onEdit = { onEditScenario(current.id, scenario.id) },
                            onDelete = { deleteTarget = scenario },
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar escenario") },
            text = { Text("¿Eliminar \"${target.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteScenario(target.id)
                    deleteTarget = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun ScenarioCard(
    scenario: MockScenario,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = onClick)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = scenario.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = scenario.statusCode.toString(),
                            color = scenario.statusCode.statusColor(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    scenario.requestSummary()?.let { request ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = request,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = scenario.responseBody.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun MockScenario.requestSummary(): String? {
    val parts = buildList {
        if (requestQuery.isNotBlank()) add("query: ${requestQuery}")
        if (requestBody.isNotBlank()) add("body: ${requestBody}")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun Int.statusColor(): Color = when {
    this in 200..299 -> Color(0xFF4CAF50)
    this in 300..399 -> Color(0xFF2196F3)
    else -> Color(0xFFF44336)
}
