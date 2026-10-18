package com.irv205.apimockinterceptor.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlag
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCharacterClick: (Int) -> Unit,
    onOpenMockServices: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val refresh = characters.loadState.refresh
    val flagsViewModel: HomeFlagsViewModel = hiltViewModel()
    val flags by flagsViewModel.flags.collectAsStateWithLifecycle()
    val mockEnabled = flags[FeatureFlag.MOCK_SERVICE] ?: FeatureFlag.MOCK_SERVICE.defaultValue

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Personajes") }) },
        floatingActionButton = {
            if (mockEnabled) {
                FloatingActionButton(onClick = onOpenMockServices) {
                    Icon(imageVector = Icons.Filled.Build, contentDescription = "Mocks")
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                refresh is LoadState.Loading && characters.itemCount == 0 -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                refresh is LoadState.Error && characters.itemCount == 0 -> {
                    ErrorState(
                        message = refresh.error.message,
                        onRetry = { characters.retry() },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                characters.itemCount == 0 -> {
                    Text(
                        text = "No hay personajes",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            count = characters.itemCount,
                            key = characters.itemKey { it.id },
                        ) { index ->
                            characters[index]?.let { character ->
                                CharacterItem(
                                    character = character,
                                    onClick = { onCharacterClick(character.id) },
                                )
                            }
                        }

                        when (val append = characters.loadState.append) {
                            is LoadState.Loading -> item { FooterLoading() }
                            is LoadState.Error -> item { FooterError(onRetry = { characters.retry() }) }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FooterError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No se pudieron cargar más personajes",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry) {
            Text(text = "Reintentar")
        }
    }
}

@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message ?: "Ocurrió un error al cargar los personajes",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRetry) {
            Text(text = "Reintentar")
        }
    }
}
