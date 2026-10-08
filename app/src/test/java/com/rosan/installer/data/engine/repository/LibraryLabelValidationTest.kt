// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.data.engine.repository

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibraryLabelValidationTest {
    @Test
    fun `generic names need companion libraries before their labels can be trusted`() {
        assertTrue(needsLibraryLabelValidation("libmain.so", emptySet()))
        assertTrue(needsLibraryLabelValidation("libapp.so", emptySet()))
        assertFalse(needsLibraryLabelValidation("libmain.so", setOf("libunity.so")))
        assertFalse(needsLibraryLabelValidation("libapp.so", setOf("libflutter.so")))
        assertTrue(needsLibraryLabelValidation("libjiagu.so", emptySet()))
        assertTrue(needsLibraryLabelValidation("libDexHelper.so", emptySet()))
        assertFalse(needsLibraryLabelValidation("libflutter.so", emptySet()))
    }
}
