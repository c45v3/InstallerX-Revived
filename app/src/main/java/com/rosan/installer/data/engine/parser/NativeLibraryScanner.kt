// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.data.engine.parser

import com.rosan.installer.domain.engine.model.packageinfo.NativeLibrary
import com.rosan.installer.domain.engine.model.source.DataEntity
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class NativeLibraryScanner(private val zipProvider: UnifiedZipFileProvider) {
    suspend fun scan(file: DataEntity.FileEntity, sourceApk: String): List<NativeLibrary> {
        val context = currentCoroutineContext()
        context.ensureActive()
        return zipProvider.open(file).use { archive ->
            archive.entries.mapNotNull { entry ->
                context.ensureActive()
                nativeLibraryEntry(entry.name, entry.isDirectory, entry.size, sourceApk)
            }
        }
    }
}

internal fun nativeLibraryEntry(path: String, isDirectory: Boolean, size: Long, sourceApk: String): NativeLibrary? {
    if (isDirectory || !path.endsWith(".so")) return null
    val parts = path.split('/')
    if (parts.any { it.isEmpty() || it == "." || it == ".." }) return null
    val abi = when {
        parts.size == 3 && parts[0] == "lib" -> parts[1]
        parts.size >= 2 && parts[0] == "assets" -> null
        else -> return null
    }
    return NativeLibrary(parts.last(), path, abi, size, sourceApk)
}
