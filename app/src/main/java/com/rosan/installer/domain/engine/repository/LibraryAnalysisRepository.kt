// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.domain.engine.repository

import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.LibraryAnalysisResult

interface LibraryAnalysisRepository {
    suspend fun analyze(apps: List<AppEntity>): LibraryAnalysisResult
}
