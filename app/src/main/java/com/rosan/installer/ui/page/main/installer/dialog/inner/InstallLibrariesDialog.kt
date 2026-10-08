// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.installer.dialog.inner

import android.content.ClipData
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosan.installer.R
import com.rosan.installer.ui.page.main.installer.InstallerViewAction
import com.rosan.installer.ui.page.main.installer.InstallerViewModel
import com.rosan.installer.ui.page.main.installer.components.LibraryPreviewContent
import com.rosan.installer.ui.page.main.installer.dialog.DialogButton
import com.rosan.installer.ui.page.main.installer.dialog.DialogInnerParams
import com.rosan.installer.ui.page.main.installer.dialog.DialogParams
import com.rosan.installer.ui.page.main.installer.dialog.dialogButtons
import kotlinx.coroutines.launch

@Composable
fun installLibrariesDialog(viewModel: InstallerViewModel): DialogParams {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler { viewModel.dispatch(InstallerViewAction.HideLibraries) }
    return DialogParams(
        title = DialogInnerParams("installer_libraries_title") { Text(stringResource(R.string.installer_libraries)) },
        content = DialogInnerParams("installer_libraries_content") {
            val context = LocalContext.current
            val clipboard = LocalClipboard.current
            val scope = rememberCoroutineScope()
            LibraryPreviewContent(
                state = uiState.libraryPreview,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                searchField = { query, onChange ->
                    OutlinedTextField(
                        value = query,
                        onValueChange = onChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.installer_libraries_search)) },
                        singleLine = true,
                    )
                },
                filterButton = { label, selected, onClick ->
                    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
                },
                message = { Text(it, style = MaterialTheme.typography.bodySmall) },
                loadingIndicator = { CircularProgressIndicator(modifier = Modifier.size(28.dp)) },
                retryButton = {
                    TextButton(onClick = { viewModel.dispatch(InstallerViewAction.RetryLibraries) }) {
                        Text(stringResource(R.string.installer_libraries_retry))
                    }
                },
                libraryRow = { library ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            scope.launch { clipboard.setClipEntry(ClipData.newPlainText("Library", library.name).toClipEntry()) }
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(painterResource(library.iconRes ?: R.drawable.ic_library_placeholder), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                val title = library.label?.let { label ->
                                    if (library.tentativeLabel) stringResource(R.string.installer_libraries_possible, label) else label
                                } ?: library.name
                                Text(title, style = MaterialTheme.typography.titleSmall)
                                if (library.label != null) Text(library.name)
                                val size = if (library.size >= 0) Formatter.formatShortFileSize(context, library.size) else "?"
                                Text("${library.abi ?: "assets"} · $size · ${library.sourceApk}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
            )
        },
        buttons = dialogButtons("installer_libraries_buttons") {
            listOf(DialogButton(stringResource(R.string.previous)) { viewModel.dispatch(InstallerViewAction.HideLibraries) })
        },
    )
}
