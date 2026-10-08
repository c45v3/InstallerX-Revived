// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.installer.components

import com.rosan.installer.domain.engine.model.packageinfo.NativeLibrary
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryPreviewFilterTest {
    private val libraries = listOf(
        NativeLibrary("libflutter.so", "lib/arm64-v8a/libflutter.so", "arm64-v8a", 1, "split.arm64.apk", "Flutter"),
        NativeLibrary("libflutter.so", "lib/x86/libflutter.so", "x86", 2, "split.x86.apk", "Flutter"),
        NativeLibrary("libcustom.so", "assets/engine/libcustom.so", null, 3, "base.apk", "自定义引擎"),
    )

    @Test
    fun `combines architecture filter with trimmed case insensitive label and source search`() {
        assertEquals(listOf(libraries[0]), filterNativeLibraries(libraries, " FLUTTER ", "arm64-v8a"))
        assertEquals(listOf(libraries[1]), filterNativeLibraries(libraries, "SPLIT.X86", ""))
        assertEquals(listOf(libraries[2]), filterNativeLibraries(libraries, "自定义", "assets"))
        assertEquals(listOf(libraries[2]), filterNativeLibraries(libraries, "engine/", ""))
        assertEquals(emptyList(), filterNativeLibraries(libraries, "flutter", "assets"))
        assertEquals(libraries, filterNativeLibraries(libraries, " ", ""))
    }
}
