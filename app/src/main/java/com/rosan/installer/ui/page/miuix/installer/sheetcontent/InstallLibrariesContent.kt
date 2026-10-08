// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.miuix.installer.sheetcontent

import android.content.ClipData
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.rosan.installer.ui.theme.miuixSheetCardColors
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun InstallLibrariesContent(viewModel: InstallerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val onBack = { viewModel.dispatch(InstallerViewAction.HideLibraries) }
    BackHandler(onBack = onBack)
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LibraryPreviewContent(
            state = uiState.libraryPreview,
            modifier = Modifier.fillMaxWidth(),
            searchField = { query, onChange ->
                TextField(
                    value = query,
                    onValueChange = onChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.installer_libraries_search),
                    singleLine = true,
                )
            },
            filterButton = { label, selected, onClick ->
                TextButton(
                    text = label,
                    onClick = onClick,
                    colors = if (selected) ButtonDefaults.textButtonColorsPrimary() else ButtonDefaults.textButtonColors(),
                )
            },
            message = { Text(it) },
            loadingIndicator = { CircularProgressIndicator(modifier = Modifier.size(28.dp)) },
            retryButton = {
                TextButton(text = stringResource(R.string.installer_libraries_retry), onClick = { viewModel.dispatch(InstallerViewAction.RetryLibraries) })
            },
            libraryRow = { library ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { clipboard.setClipEntry(ClipData.newPlainText("Library", library.name).toClipEntry()) }
                    },
                    colors = miuixSheetCardColors(),
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
                            Text(title)
                            if (library.label != null) Text(library.name)
                            val size = if (library.size >= 0) Formatter.formatShortFileSize(context, library.size) else "?"
                            Text("${library.abi ?: "assets"} · $size · ${library.sourceApk}")
                        }
                    }
                }
            },
        )
        TextButton(
            text = stringResource(R.string.previous),
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 12.dp),
        )
    }
}
