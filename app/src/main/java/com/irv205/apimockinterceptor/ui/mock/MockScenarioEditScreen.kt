package com.irv205.apimockinterceptor.ui.mock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockScenarioEditScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: MockScenarioEditViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (form.name.isEmpty()) "Nuevo escenario" else "Editar escenario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save(onDone) }) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Guardar")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = form.name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
            )
            OutlinedTextField(
                value = form.statusCode,
                onValueChange = viewModel::setStatusCode,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Status code") },
            )
            OutlinedTextField(
                value = form.contentType,
                onValueChange = viewModel::setContentType,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Content-Type") },
            )
            OutlinedTextField(
                value = form.requestQuery,
                onValueChange = viewModel::setRequestQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Request query (opcional, ej: page=1)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = form.requestBody,
                onValueChange = viewModel::setRequestBody,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Request body (opcional)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = form.responseHeaders,
                onValueChange = viewModel::setResponseHeaders,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Response headers (opcional, una por línea: Key: Value)") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = form.responseBody,
                onValueChange = viewModel::setResponseBody,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Response body") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                minLines = 6,
                maxLines = 12,
            )
            OutlinedTextField(
                value = form.delayMs,
                onValueChange = viewModel::setDelayMs,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Delay ms (opcional)") },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Regla: si no hay request query ni body, el escenario no se usa por matching específico; caerá al escenario seleccionado.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
