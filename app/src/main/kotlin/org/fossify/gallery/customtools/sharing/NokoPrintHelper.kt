package org.fossify.gallery.customtools.sharing

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object NokoPrintHelper {

    private const val PACKAGE =
        "com.nokoprint"

    fun print(
        context: Context,
        path: String
    ): Boolean {

        val file = File(path)

        if (!file.exists()) {
            return false
        }

        return try {

            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

            val mimeType =
                when {
                    path.endsWith(
                        ".pdf",
                        true
                    ) ->
                        "application/pdf"

                    path.endsWith(
                        ".png",
                        true
                    ) ->
                        "image/png"

                    path.endsWith(
                        ".webp",
                        true
                    ) ->
                        "image/webp"

                    else ->
                        "image/jpeg"
                }

            val intent =
                Intent(Intent.ACTION_SEND).apply {

                    type = mimeType

                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    setPackage(PACKAGE)
                }

            context.startActivity(intent)

            true

        } catch (e: Exception) {
            false
        }
    }
}
