package org.fossify.gallery.customtools.layout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

object PassportPhotoLayoutEngine {

    const val PHOTO_WIDTH_MM = 30f
    const val PHOTO_HEIGHT_MM = 40f
    const val COLUMNS = 6

    fun createPassportPhoto(source: Bitmap): Bitmap {
        val targetRatio = PHOTO_WIDTH_MM / PHOTO_HEIGHT_MM
        val sourceRatio = source.width.toFloat() / source.height

        val crop = if (sourceRatio > targetRatio) {
            val cropWidth = (source.height * targetRatio).toInt()

            Bitmap.createBitmap(
                source,
                (source.width - cropWidth) / 2,
                0,
                cropWidth,
                source.height
            )
        } else {
            val cropHeight = (source.width / targetRatio).toInt()

            Bitmap.createBitmap(
                source,
                0,
                (source.height - cropHeight) / 2,
                source.width,
                cropHeight
            )
        }

        val width = A4Page.mmToPx(PHOTO_WIDTH_MM).toInt()
        val height = A4Page.mmToPx(PHOTO_HEIGHT_MM).toInt()

        return Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        ).also { output ->

            Canvas(output).drawBitmap(
                crop,
                null,
                Rect(0, 0, width, height),
                Paint(
                    Paint.ANTI_ALIAS_FLAG or
                        Paint.FILTER_BITMAP_FLAG
                )
            )

            if (crop !== source) {
                crop.recycle()
            }
        }
    }

    fun createA4Sheet(
        photo: Bitmap,
        rows: Int = 1,
        sideMarginMm: Float = 0.5f,
        topMarginMm: Float = 8f,
        horizontalGapMm: Float = 0.5f,
        verticalGapMm: Float = 0.5f
    ): Bitmap {

        val pageWidth = A4Page.WIDTH_PX
        val pageHeight = A4Page.HEIGHT_PX

        val result = Bitmap.createBitmap(
            pageWidth,
            pageHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        /*
         * IMPORTANT:
         *
         * Photo size remains FIXED:
         *
         * 30 × 40 mm
         *
         * Gap is separate from the photo.
         */
        val photoWidth =
            A4Page.mmToPx(PHOTO_WIDTH_MM)

        val photoHeight =
            A4Page.mmToPx(PHOTO_HEIGHT_MM)

        val sideMargin =
            A4Page.mmToPx(sideMarginMm)

        val horizontalGap =
            A4Page.mmToPx(horizontalGapMm)

        val verticalGap =
            A4Page.mmToPx(verticalGapMm)

        val topMargin =
            A4Page.mmToPx(topMarginMm)

        /*
         * Exact six-column width.
         *
         * 6 × 30mm + 5 × 1mm
         * = 185mm
         *
         * Therefore the photos stay exactly
         * 30mm wide and the 1mm gaps remain.
         */
        val gridWidth =
            COLUMNS * photoWidth +
                (COLUMNS - 1) * horizontalGap

        /*
         * Center the complete grid on A4.
         */
        val startX =
            (pageWidth - gridWidth) / 2f

        val actualRows =
            rows.coerceIn(1, 6)

        val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG or
                    Paint.FILTER_BITMAP_FLAG
            )

        /*
         * Black border.
         *
         * 4px = doubled thickness.
         */
        val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.BLACK
            }

        for (row in 0 until actualRows) {

            for (column in 0 until COLUMNS) {

                /*
                 * GAP IS HERE.
                 *
                 * Each next photo starts after:
                 *
                 * photo width + gap
                 */
                val left =
                    startX +
                        column *
                        (photoWidth + horizontalGap)

                val top =
                    topMargin +
                        row *
                        (photoHeight + verticalGap)

                val right =
                    left + photoWidth

                val bottom =
                    top + photoHeight

                val rect =
                    Rect(
                        left.toInt(),
                        top.toInt(),
                        right.toInt(),
                        bottom.toInt()
                    )

                /*
                 * Draw the actual 30 × 40 mm photo.
                 */
                canvas.drawBitmap(
                    photo,
                    null,
                    rect,
                    paint
                )

                /*
                 * Black border around each photo.
                 */
                canvas.drawRect(
                    rect,
                    borderPaint
                )
            }
        }

        return result
    }
}

