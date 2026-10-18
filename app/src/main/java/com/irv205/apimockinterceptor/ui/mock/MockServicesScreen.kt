package com.irv205.apimockinterceptor.ui.mock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.irv205.apimockinterceptor.data.mock.MockService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockServicesScreen(
    onBack: () -> Unit,
    onServiceClick: (String) -> Unit,
    onCreateService: () -> Unit,
    onEditService: (String) -> Unit,
    viewModel: MockServicesViewModel = hiltViewModel(),
) {
    val services by viewModel.services.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    var menuExpanded by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<MockService?>(null) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Mock service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Más opciones",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exportar JSON") },
                            onClick = {
                                menuExpanded = false
                                showExport = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Importar JSON") },
                            onClick = {
                                menuExpanded = false
                                importText = ""
                                importError = null
                                showImport = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Restaurar por defecto") },
                            onClick = {
                                menuExpanded = false
                                viewModel.resetToDefaults()
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateService) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Nuevo servicio")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = services, key = { it.id }) { service ->
                ServiceCard(
                    service = service,
                    onToggle = { viewModel.setEnabled(service.id, it) },
                    onClick = { onServiceClick(service.id) },
                    onEdit = { onEditService(service.id) },
                    onDelete = { deleteTarget = service },
                )
            }
        }
    }

    if (showExport) {
        val json = remember(showExport) { viewModel.exportJson() }
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Exportar mocks") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = json,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(json))
                    showExport = false
                }) { Text("Copiar") }
            },
            dismissButton = {
                TextButton(onClick = { showExport = false }) { Text("Cerrar") }
            },
        )
    }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("Importar mocks") },
            text = {
                Column {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = {
                            importText = it
                            importError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        label = { Text("JSON de servicios") },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    )
                    importError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = {
                        clipboard.getText()?.text?.let { importText = it }
                    }) { Text("Pegar") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val result = viewModel.importJson(importText)
                    result.onSuccess { showImport = false }
                        .onFailure { importError = it.message ?: "Error al importar" }
                }) { Text("Importar") }
            },
            dismissButton = {
                TextButton(onClick = { showImport = false }) { Text("Cancelar") }
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar servicio") },
            text = { Text("¿Eliminar \"${target.name}\" y sus escenarios?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
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
private fun ServiceCard(
    service: MockService,
    onToggle: (Boolean) -> Unit,
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${service.method} ${service.pathPattern}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = service.enabled, onCheckedChange = onToggle)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Escenario activo: ${service.selectedScenario()?.name ?: "—"}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
