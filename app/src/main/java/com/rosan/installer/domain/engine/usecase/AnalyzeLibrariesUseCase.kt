// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.domain.engine.usecase

import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.repository.LibraryAnalysisRepository

class AnalyzeLibrariesUseCase(private val repository: LibraryAnalysisRepository) {
    suspend operator fun invoke(apps: List<AppEntity>) = repository.analyze(
        apps.filter { it is AppEntity.BaseEntity || it is AppEntity.SplitEntity },
    )
}
