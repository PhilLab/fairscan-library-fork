/*
 * Copyright 2025-2026 The FairScan authors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.fairscan.app

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

// Settings that only exist because :app can be embedded as a library: they are read from the host
// app's manifest <meta-data>. Kept in their own file to keep FairScanApp.kt close to upstream.

/**
 * The manifest <meta-data> key that specifies the subdirectory name under filesDir/cacheDir
 * which FairScan can use for file storage
 */
const val STORAGE_SUBDIR_KEY = "org.fairscan.app.STORAGE_SUBDIR"

/**
 * Resolve the FairScan storage subdirectory name from the manifest <meta-data> value, or throw if
 * absent. An empty string is valid, meaning FairScan may use filesDir/cacheDir directly.
 */
internal fun resolveStorageSubdir(metaDataValue: String?): String =
    metaDataValue ?: throw IllegalStateException(
        "FairScan requires a <meta-data android:name=\"$STORAGE_SUBDIR_KEY\" " +
            "android:value=\"...\" /> entry in your app's <application> manifest tag - the " +
            "subdirectory FairScan may use under your filesDir/cacheDir, or \"\" to use them " +
            "directly. See README.md."
    )

internal fun storageRoot(dir: File, subdir: String): File =
    if (subdir.isEmpty()) dir else File(dir, subdir)

internal fun readStorageSubdirMetaData(context: Context): String? =
    context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData?.getString(STORAGE_SUBDIR_KEY)

/**
 * The optional manifest <meta-data> boolean key controlling whether the About screen shows the
 * "Support" section. Defaults to true when absent.
 */
const val SHOW_SUPPORT_KEY = "org.fairscan.app.SHOW_SUPPORT"

internal fun readShowSupportMetaData(context: Context): Boolean =
    context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData?.getBoolean(SHOW_SUPPORT_KEY, true) ?: true
