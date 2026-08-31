package org.fossify.gallery.customtools.preview

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import org.fossify.gallery.customtools.sharing.NokoPrintHelper
import org.fossify.gallery.customtools.storage.DownloadStorageHelper
import java.io.File

object ResultDialog {

    fun show(
        context: Context,
        path: String,
        title: String = "Result"
    ) {

        val file = File(path)

        if (!file.exists()) {
            Toast.makeText(
                context,
                "Result file not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dialog = Dialog(context)

        val root =
            LinearLayout(context).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    20,
                    20,
                    20,
                    20
                )
            }

        val preview =
            ImageView(context).apply {

                adjustViewBounds = true

                scaleType =
                    ImageView.ScaleType.FIT_CENTER

                if (
                    path.endsWith(
                        ".pdf",
                        true
                    )
                ) {
                    setImageResource(
                        android.R.drawable.ic_menu_view
                    )
                } else {
                    setImageBitmap(
                        BitmapFactory.decodeFile(path)
                    )
                }
            }

        root.addView(
            preview,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 480)
            )
        )

        val buttons =
            LinearLayout(context).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(context, 12),
                    0,
                    0
                )
            }

        val print =
            Button(context).apply {
                text = "🖨 NokoPrint"

                setOnClickListener {

                    val success =
                        NokoPrintHelper.print(
                            context,
                            path
                        )

                    if (!success) {
                        Toast.makeText(
                            context,
                            "NokoPrint is not installed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        val save =
            Button(context).apply {
                text = "↓ Save"

                setOnClickListener {

                    val extension =
                        if (
                            path.endsWith(
                                ".pdf",
                                true
                            )
                        ) {
                            "pdf"
                        } else {
                            "png"
                        }

                    val mime =
                        if (extension == "pdf") {
                            "application/pdf"
                        } else {
                            "image/png"
                        }

                    val saved =
                        DownloadStorageHelper
                            .saveToDownloads(
                                context,
                                path,
                                "Fossify_${System.currentTimeMillis()}.$extension",
                                mime
                            )

                    Toast.makeText(
                        context,
                        if (saved) {
                            "Saved to Downloads"
                        } else {
                            "Could not save file"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        val open =
            Button(context).apply {
                text = "↗ Open"

                setOnClickListener {

                    try {

                        val uri =
                            FileProvider
                                .getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )

                        val mime =
                            if (
                                path.endsWith(
                                    ".pdf",
                                    true
                                )
                            ) {
                                "application/pdf"
                            } else {
                                "image/*"
                            }

                        val intent =
                            Intent(
                                Intent.ACTION_VIEW
                            ).apply {

                                setDataAndType(
                                    uri,
                                    mime
                                )

                                addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            }

                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                "Open with"
                            )
                        )

                    } catch (e: Exception) {

                        Toast.makeText(
                            context,
                            "No compatible app found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        buttons.addView(print)
        buttons.addView(save)
        buttons.addView(open)

        root.addView(buttons)

        dialog.setTitle(title)
        dialog.setContentView(root)

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.white
        )

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
