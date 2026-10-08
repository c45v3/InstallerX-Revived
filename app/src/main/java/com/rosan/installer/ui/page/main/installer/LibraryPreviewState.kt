// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.installer

import com.rosan.installer.domain.engine.model.packageinfo.LibraryAnalysisResult

data class LibraryPreviewState(
    val loading: Boolean = false,
    val result: LibraryAnalysisResult? = null,
    val failed: Boolean = false,
)
