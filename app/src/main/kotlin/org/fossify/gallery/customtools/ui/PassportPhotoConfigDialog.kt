package org.fossify.gallery.customtools.ui

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.fossify.gallery.customtools.layout.PassportPhotoLayoutEngine
import org.fossify.gallery.customtools.preview.ResultDialog
import org.fossify.gallery.customtools.storage.DownloadStorageHelper
import java.io.FileOutputStream

object PassportPhotoConfigDialog {

    fun show(
        context: Context,
        imagePath: String
    ) {
        val dialog = Dialog(context)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, 20),
                dp(context, 20),
                dp(context, 20),
                dp(context, 20)
            )

            background = roundedBackground(
                Color.WHITE,
                dp(context, 24)
            )
        }

        val title = TextView(context).apply {
            text = "Passport Photo"
            textSize = 22f
            setTextColor(Color.BLACK)
        }

        root.addView(title)

        val subtitle = TextView(context).apply {
            text = "35 × 45 mm  •  A4 sheet"
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(
                0,
                dp(context, 4),
                0,
                dp(context, 18)
            )
        }

        root.addView(subtitle)

        val rowLabel = TextView(context).apply {
            text = "Number of rows"
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        root.addView(rowLabel)

        val rowsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        var selectedRows = 1

        val rowButtons = mutableListOf<TextView>()

        for (row in 1..6) {

            val button = TextView(context).apply {
                text = row.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(
                    if (row == 1) Color.WHITE
                    else Color.DKGRAY
                )

                background = roundedBackground(
                    if (row == 1)
                        Color.rgb(55, 95, 220)
                    else
                        Color.rgb(240, 240, 240),
                    dp(context, 12)
                )

                setOnClickListener {

                    selectedRows = row

                    rowButtons.forEachIndexed { index, item ->

                        val selected =
                            index + 1 == selectedRows

                        item.setTextColor(
                            if (selected)
                                Color.WHITE
                            else
                                Color.DKGRAY
                        )

                        item.background =
                            roundedBackground(
                                if (selected)
                                    Color.rgb(55, 95, 220)
                                else
                                    Color.rgb(240, 240, 240),
                                dp(context, 12)
                            )
                    }
                }
            }

            rowButtons.add(button)

            rowsContainer.addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    dp(context, 48),
                    1f
                ).apply {
                    setMargins(
                        dp(context, 3),
                        dp(context, 8),
                        dp(context, 3),
                        dp(context, 8)
                    )
                }
            )
        }

        root.addView(rowsContainer)

        val marginLabel = TextView(context).apply {
            text = "Side margin (mm)"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(
                0,
                dp(context, 12),
                0,
                dp(context, 4)
            )
        }

        root.addView(marginLabel)

        val marginInput = EditText(context).apply {
            setText("0.5")
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        root.addView(
            marginInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 50)
            )
        )

        val gapLabel = TextView(context).apply {
            text = "Photo gap (mm)"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(
                0,
                dp(context, 12),
                0,
                dp(context, 4)
            )
        }

        root.addView(gapLabel)

        val gapInput = EditText(context).apply {
            setText("0.5")
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        root.addView(
            gapInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 50)
            )
        )

        val generate = Button(context).apply {

            text = "Create Preview"

            setOnClickListener {

                val sideMargin =
                    marginInput.text
                        .toString()
                        .toFloatOrNull()
                        ?.coerceIn(0f, 20f)
                        ?: 8f

                val gap =
                    gapInput.text
                        .toString()
                        .toFloatOrNull()
                        ?.coerceIn(0f, 10f)
                        ?: 0f

                val source =
                    BitmapFactory.decodeFile(
                        imagePath
                    )

                if (source == null) {

                    Toast.makeText(
                        context,
                        "Unable to open image",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val photo =
                    PassportPhotoLayoutEngine
                        .createPassportPhoto(
                            source
                        )

                val sheet =
                    PassportPhotoLayoutEngine
                        .createA4Sheet(
                            photo = photo,
                            rows = selectedRows,
                            sideMarginMm = sideMargin,
                            topMarginMm = 8f,
                            horizontalGapMm = gap,
                            verticalGapMm = gap
                        )

                val file =
                    DownloadStorageHelper
                        .getPrivateOutputFile(
                            context,
                            "passport_${System.currentTimeMillis()}.png"
                        )

                FileOutputStream(file).use {
                    sheet.compress(
                        android.graphics.Bitmap.CompressFormat.PNG,
                        100,
                        it
                    )
                }

                source.recycle()
                photo.recycle()
                sheet.recycle()

                dialog.dismiss()

                ResultDialog.show(
                    context,
                    file.absolutePath,
                    "Passport Photos — A4"
                )
            }
        }

        root.addView(
            generate,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 52)
            ).apply {
                topMargin = dp(context, 16)
            }
        )

        dialog.setContentView(root)

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setGravity(
            Gravity.BOTTOM
        )
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun dp(
        context: Context,
        value: Int
    ): Int {
        return (
            value *
                context.resources.displayMetrics.density
            ).toInt()
    }
}


