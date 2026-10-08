// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.data.engine.parser

import com.rosan.installer.domain.engine.model.source.DataEntity
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class NativeLibraryScannerTest {
    private val scanner = NativeLibraryScanner(UnifiedZipFileProvider(CommonsZipFileProvider(), SeekableZipReader()))

    @Test
    fun `reads compressed and stored libraries with ABI size and source without parsing their payloads`() = runTest {
        val file = Files.createTempFile("library-scan", ".apk").toFile()
        try {
            ZipOutputStream(file.outputStream()).use { zip ->
                for (path in listOf("lib/arm64-v8a/libflutter.so", "lib/x86/libflutter.so", "assets/engine/libcustom.so", "res/raw/other.so", "classes.dex")) {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(byteArrayOf(1, 2, 3))
                    zip.closeEntry()
                }
                zip.putNextEntry(
                    ZipEntry("lib/armeabi-v7a/libempty.so").apply {
                        method = ZipEntry.STORED
                        size = 0
                        compressedSize = 0
                        crc = 0
                    },
                )
                zip.closeEntry()
            }
            val results = scanner.scan(DataEntity.FileEntity(file.path), "split.arm64.apk")
            assertEquals(listOf("arm64-v8a", "x86", null, "armeabi-v7a"), results.map { it.abi })
            assertEquals(listOf(3L, 3L, 3L, 0L), results.map { it.size })
            assertEquals(2, results.count { it.name == "libflutter.so" })
            assertEquals(listOf("split.arm64.apk"), results.map { it.sourceApk }.distinct())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `rejects directories unrelated paths and traversal but retains unknown ABI and unknown size`() {
        for (path in listOf("lib/arm64-v8a/../libx.so", "assets/./libx.so", "assets//libx.so", "lib/libx.so", "lib/arm64-v8a/nested/libx.so", "res/libx.so", "lib/arm64-v8a/libx.so.backup")) {
            assertNull(nativeLibraryEntry(path, false, 1, "base.apk"), path)
        }
        assertNull(nativeLibraryEntry("assets/libx.so", true, 1, "base.apk"))
        val unknown = nativeLibraryEntry("lib/future-abi/libx.so", false, -1, "base.apk")!!
        assertEquals("future-abi", unknown.abi)
        assertEquals(-1, unknown.size)
    }

    @Test
    fun `cancelled scan never attempts to open the APK`() = runTest {
        val cancelled = Job().apply { cancel() }
        assertFailsWith<CancellationException> {
            withContext(cancelled) { scanner.scan(DataEntity.FileEntity("missing.apk"), "base.apk") }
        }
    }
}
