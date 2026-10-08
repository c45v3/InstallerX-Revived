// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.installer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rosan.installer.R
import com.rosan.installer.domain.engine.model.packageinfo.NativeLibrary
import com.rosan.installer.ui.page.main.installer.LibraryPreviewState

// These slots are stateless renderers for multiple filter/message rows, not movable content.
@Suppress("ktlint:compose:content-slot-reused")
@Composable
fun LibraryPreviewContent(
    state: LibraryPreviewState,
    searchField: @Composable (String, (String) -> Unit) -> Unit,
    filterButton: @Composable (String, Boolean, () -> Unit) -> Unit,
    message: @Composable (String) -> Unit,
    libraryRow: @Composable (NativeLibrary) -> Unit,
    retryButton: @Composable () -> Unit,
    loadingIndicator: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var abi by remember { mutableStateOf("") }
    val result = state.result
    val libraries = result?.libraries.orEmpty()
    val abis = remember(libraries) { libraries.map { it.abi ?: "assets" }.distinct().sorted() }
    val activeAbi = abi.takeIf { it in abis }.orEmpty()
    val filtered = remember(libraries, query, activeAbi) { filterNativeLibraries(libraries, query, activeAbi) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        searchField(query) { query = it }
        if (abis.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                filterButton(stringResource(R.string.installer_libraries_all), activeAbi.isEmpty()) { abi = "" }
                abis.forEach { value -> filterButton(value, activeAbi == value) { abi = value } }
            }
        }
        when {
            state.loading -> loadingIndicator()

            state.failed -> {
                message(stringResource(R.string.installer_libraries_failed))
                retryButton()
            }

            result != null -> {
                if (result.failedApks.isNotEmpty()) {
                    message(stringResource(R.string.installer_libraries_partial, result.failedApks.joinToString()))
                }
                if (result.rulesUnavailable) message(stringResource(R.string.installer_libraries_rules_unavailable))
                if (result.failedApks.isNotEmpty() || result.rulesUnavailable) retryButton()
                message(stringResource(R.string.installer_libraries_count, filtered.size, libraries.size))
                if (filtered.isEmpty()) {
                    message(stringResource(if (libraries.isEmpty()) R.string.installer_libraries_empty else R.string.installer_libraries_no_matches))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filtered) { library -> libraryRow(library) }
                    }
                }
            }
        }
    }
}

internal fun filterNativeLibraries(libraries: List<NativeLibrary>, query: String, abi: String): List<NativeLibrary> {
    val text = query.trim()
    return libraries.filter { library ->
        (abi.isEmpty() || (library.abi ?: "assets") == abi) &&
            (
                text.isEmpty() || listOf(library.name, library.label.orEmpty(), library.archivePath, library.sourceApk)
                    .any { it.contains(text, ignoreCase = true) }
                )
    }
}
