// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.data.engine.repository

import android.content.Context
import com.absinthe.rulesbundle.LCRules
import com.absinthe.rulesbundle.NATIVE
import com.rosan.installer.data.engine.parser.NativeLibraryScanner
import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.LibraryAnalysisResult
import com.rosan.installer.domain.engine.model.packageinfo.NativeLibrary
import com.rosan.installer.domain.engine.model.source.DataEntity
import com.rosan.installer.domain.engine.repository.LibraryAnalysisRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber

class LibraryAnalysisRepositoryImpl(
    private val context: Context,
    private val scanner: NativeLibraryScanner,
) : LibraryAnalysisRepository {
    private var rulesInitialized = false

    override suspend fun analyze(apps: List<AppEntity>): LibraryAnalysisResult = withContext(Dispatchers.IO) {
        val libraries = mutableListOf<NativeLibrary>()
        val failedApks = mutableListOf<String>()
        for (app in apps) {
            currentCoroutineContext().ensureActive()
            try {
                val file = app.data as? DataEntity.FileEntity ?: error("APK source is not seekable")
                libraries += scanner.scan(file, app.name)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.w(error, "Library scan failed for %s", app.name)
                failedApks += app.name
            }
        }
        var rulesUnavailable = false
        val matches = mutableMapOf<String, com.absinthe.rulesbundle.Rule?>()
        if (libraries.isNotEmpty()) {
            try {
                synchronized(this@LibraryAnalysisRepositoryImpl) {
                    if (!rulesInitialized) {
                        LCRules.init(context)
                        rulesInitialized = true
                    }
                }
                val language = context.resources.configuration.locales[0].language
                for (name in libraries.map { it.name }.distinct()) {
                    currentCoroutineContext().ensureActive()
                    matches[name] = LCRules.getRule(name, NATIVE, useRegex = true, language = language)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.w(error, "Library rules unavailable; showing raw library entries")
                rulesUnavailable = true
            }
        }
        val nativeNames = libraries.filter { it.abi != null }.map { it.name }.toSet()
        LibraryAnalysisResult(
            libraries = libraries.map { library ->
                val rule = matches[library.name]
                library.copy(
                    label = rule?.label,
                    iconRes = rule?.iconRes,
                    tentativeLabel = rule != null && needsLibraryLabelValidation(library.name, nativeNames),
                )
            }.sortedWith(compareBy({ it.abi ?: "assets" }, { it.name }, { it.sourceApk }, { it.archivePath })),
            failedApks = failedApks,
            rulesUnavailable = rulesUnavailable,
        )
    }
}

internal fun needsLibraryLabelValidation(name: String, nativeNames: Set<String>): Boolean = when (name) {
    "libapp.so" -> "libflutter.so" !in nativeNames

    "libmain.so" -> "libunity.so" !in nativeNames

    "libjiagu.so", "libjiagu_a64.so", "libjiagu_x86.so", "libjiagu_x64.so",
    "libDexHelper.so", "libDexHelper-x86.so", "libdexjni.so",
    -> true

    else -> false
}
