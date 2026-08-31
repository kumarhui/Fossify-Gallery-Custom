package org.fossify.gallery.customtools.ui

import org.fossify.gallery.R

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.fossify.gallery.customtools.conversion.ImageConverter
import org.fossify.gallery.customtools.conversion.PdfConverter
import org.fossify.gallery.customtools.info.ResultInfoHelper
import org.fossify.gallery.customtools.layout.IdCardLayoutEngine
import org.fossify.gallery.customtools.layout.PassportPhotoLayoutEngine
import org.fossify.gallery.customtools.preview.ResultDialog
import org.fossify.gallery.customtools.sharing.NokoPrintHelper
import org.fossify.gallery.customtools.sharing.WhatsAppHelper
import org.fossify.gallery.customtools.storage.DownloadStorageHelper
import java.io.FileOutputStream

object CustomToolsSheet {

    fun show(
        context: Context,
        imagePath: String
    ) {

        val items = listOf(

            CustomToolsItem(
                ToolId.ID_CARD,
                "ID Card",
                "85.6 × 54 mm on A4",
                R.drawable.ic_tool_id_card
            ),

            CustomToolsItem(
                ToolId.PASSPORT_PHOTO,
                "Passport Photo",
                "35 × 45 mm • 6 columns",
                R.drawable.ic_tool_passport
            ),

            CustomToolsItem(
                ToolId.A4_LAYOUT,
                "A4 Layout",
                "Place image on A4",
                R.drawable.ic_tool_a4
            ),

            CustomToolsItem(
                ToolId.CONVERT_IMAGE,
                "Convert Image",
                "JPEG • PNG • WEBP",
                R.drawable.ic_tool_convert_image
            ),

            CustomToolsItem(
                ToolId.CONVERT_PDF,
                "Convert to PDF",
                "Create A4 PDF",
                R.drawable.ic_tool_pdf
            ),

            CustomToolsItem(
                ToolId.IMAGE_INFO,
                "Image Information",
                "Size • resolution • date",
                R.drawable.ic_tool_info
            ),

            CustomToolsItem(
                ToolId.WHATSAPP,
                "WhatsApp",
                "Share image",
                R.drawable.ic_tool_whatsapp
            ),

            CustomToolsItem(
                ToolId.NOKOPRINT,
                "NokoPrint",
                "Print with NokoPrint",
                R.drawable.ic_tool_print
            )
        )
        val dialog =
            AlertDialog.Builder(context)
                .setTitle("Custom Tools")
                .setItems(
                    items.map {
                        "${it.title}"
                    }.toTypedArray()
                ) { dialog, which ->

                    dialog.dismiss()

                    execute(
                        context,
                        items[which].id,
                        imagePath
                    )
                }
                .create()

        dialog.show()
    }

    private fun execute(
        context: Context,
        tool: ToolId,
        imagePath: String
    ) {

        when (tool) {

            ToolId.ID_CARD ->
                idCard(
                    context,
                    imagePath
                )

            ToolId.PASSPORT_PHOTO ->
                PassportPhotoConfigDialog.show(
                    context,
                    imagePath
                )

            ToolId.A4_LAYOUT ->
                a4(
                    context,
                    imagePath
                )

            ToolId.CONVERT_IMAGE ->
                convertImage(
                    context,
                    imagePath
                )

            ToolId.CONVERT_PDF ->
                convertPdf(
                    context,
                    imagePath
                )

            ToolId.IMAGE_INFO ->
                info(
                    context,
                    imagePath
                )

            ToolId.WHATSAPP ->
                whatsapp(
                    context,
                    imagePath
                )

            ToolId.NOKOPRINT ->
                nokoPrint(
                    context,
                    imagePath
                )
        }
    }

    private fun idCard(
        context: Context,
        path: String
    ) {

        val source =
            BitmapFactory.decodeFile(path)
                ?: return

        val result =
            IdCardLayoutEngine.createA4Sheet(
                source
            )

        val file =
            DownloadStorageHelper
                .getPrivateOutputFile(
                    context,
                    "id_card_${System.currentTimeMillis()}.png"
                )

        FileOutputStream(file).use {
            result.compress(
                android.graphics.Bitmap.CompressFormat.PNG,
                100,
                it
            )
        }

        source.recycle()
        result.recycle()

        ResultDialog.show(
            context,
            file.absolutePath,
            "ID Card â€” A4"
        )
    }

    private fun a4(
        context: Context,
        path: String
    ) {

        // A4 Layout currently uses the
        // image-to-A4 PDF engine.
        // It stops at ResultDialog.
        convertPdf(
            context,
            path
        )
    }

    private fun convertImage(
        context: Context,
        path: String
    ) {

        val formats =
            arrayOf(
                "JPEG",
                "PNG",
                "WEBP"
            )

        AlertDialog.Builder(context)
            .setTitle("Convert Image")
            .setItems(formats) { _, which ->

                val format =
                    when (which) {
                        0 ->
                            ImageConverter.Format.JPEG

                        1 ->
                            ImageConverter.Format.PNG

                        else ->
                            ImageConverter.Format.WEBP
                    }

                val result =
                    ImageConverter.convert(
                        context,
                        path,
                        format
                    )

                if (
                    result.success &&
                    result.outputPath != null
                ) {

                    ResultDialog.show(
                        context,
                        result.outputPath,
                        "Converted Image"
                    )

                } else {

                    Toast.makeText(
                        context,
                        result.error
                            ?: "Conversion failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun convertPdf(
        context: Context,
        path: String
    ) {

        val result =
            PdfConverter.convert(
                context,
                path
            )

        if (
            result.success &&
            result.outputPath != null
        ) {

            ResultDialog.show(
                context,
                result.outputPath,
                "PDF Result"
            )

        } else {

            Toast.makeText(
                context,
                result.error
                    ?: "PDF conversion failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun info(
        context: Context,
        path: String
    ) {

        val info =
            ResultInfoHelper.getInfo(path)
                ?: return

        AlertDialog.Builder(context)
            .setTitle(info.name)
            .setMessage(
                """
                Resolution: ${info.width} Ã— ${info.height}

                Size: ${formatSize(info.size)}

                Modified: ${info.modified}

                Path:
                ${info.path}
                """.trimIndent()
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun whatsapp(
        context: Context,
        path: String
    ) {

        if (
            !WhatsAppHelper.share(
                context,
                path
            )
        ) {

            Toast.makeText(
                context,
                "WhatsApp is not installed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun nokoPrint(
        context: Context,
        path: String
    ) {

        if (
            !NokoPrintHelper.print(
                context,
                path
            )
        ) {

            Toast.makeText(
                context,
                "NokoPrint is not installed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatSize(
        size: Long
    ): String {

        return when {

            size < 1024 ->
                "$size B"

            size < 1024 * 1024 ->
                "%.1f KB".format(
                    size / 1024f
                )

            else ->
                "%.1f MB".format(
                    size /
                        (1024f * 1024f)
                )
        }
    }

    private fun dp(
        context: Context,
        value: Int
    ): Int =
        (
            value *
                context.resources.displayMetrics.density
            ).toInt()
}







