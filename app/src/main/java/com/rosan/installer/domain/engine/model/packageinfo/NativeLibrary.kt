// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.domain.engine.model.packageinfo

data class NativeLibrary(
    val name: String,
    val archivePath: String,
    val abi: String?,
    val size: Long,
    val sourceApk: String,
    val label: String? = null,
    val iconRes: Int? = null,
    val tentativeLabel: Boolean = false,
)

data class LibraryAnalysisResult(
    val libraries: List<NativeLibrary> = emptyList(),
    val failedApks: List<String> = emptyList(),
    val rulesUnavailable: Boolean = false,
)
