package com.eewill.discgolftraining.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object ImageFiles {
    fun roundsDir(context: Context): File =
        File(context.filesDir, "rounds").apply { if (!exists()) mkdirs() }

    fun createRoundImageFile(context: Context): File {
        val name = "${UUID.randomUUID()}.jpg"
        return File(roundsDir(context), name)
    }

    fun fileProviderUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
