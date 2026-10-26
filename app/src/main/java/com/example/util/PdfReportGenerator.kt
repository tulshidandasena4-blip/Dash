package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.AggregateReportSummary
import com.example.data.model.BusinessInfo
import com.example.data.model.DailyReportRow
import com.example.data.model.ExpenditureEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateReportPdf(
        context: Context,
        reportTitle: String,
        periodText: String,
        rows: List<DailyReportRow>,
        summary: AggregateReportSummary,
        expenditures: List<ExpenditureEntity> = emptyList()
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        paint.isAntiAlias = true

        // Background
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

        // Top Header Accent Banner
        paint.color = Color.rgb(15, 107, 79) // Emerald brand
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(BusinessInfo.NAME.uppercase(), pageWidth / 2f, 32f, paint)

        // Subheader Tagline
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        canvas.drawText(BusinessInfo.TAGLINE, pageWidth / 2f, 48f, paint)

        // Address & Mobile in Banner
        paint.textSize = 8.5f
        canvas.drawText("${BusinessInfo.ADDRESS}  •  Mob: ${BusinessInfo.MOBILE}", pageWidth / 2f, 62f, paint)

        // Report Sub-Bar
        var currentY = 92f
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(reportTitle.uppercase(), 20f, currentY, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.RIGHT
        val generatedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated: $generatedAt", pageWidth - 20f, currentY, paint)

        currentY += 14f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(15, 107, 79)
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Period: $periodText", 20f, currentY, paint)

        currentY += 12f
        // Divider line
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1f
        canvas.drawLine(20f, currentY, pageWidth - 20f, currentY, paint)

        // 4 Summary KPI Cards
        currentY += 12f
        val cardWidth = (pageWidth - 40f - 18f) / 4f
        val cardHeight = 44f

        drawKpiCard(canvas, 20f, currentY, cardWidth, cardHeight, "TOTAL VEHICLES", summary.totalVehicles.toString(), Color.rgb(14, 116, 144))
        drawKpiCard(canvas, 20f + cardWidth + 6f, currentY, cardWidth, cardHeight, "TOTAL COLLECTION", FormatUtils.formatCurrency(summary.grandTotalCollection), Color.rgb(15, 107, 79))
        drawKpiCard(canvas, 20f + (cardWidth + 6f) * 2, currentY, cardWidth, cardHeight, "TOTAL EXPENDITURE", FormatUtils.formatCurrency(summary.totalOthersExpenditure), Color.rgb(220, 38, 38))
        drawKpiCard(canvas, 20f + (cardWidth + 6f) * 3, currentY, cardWidth, cardHeight, "NET COLLECTION", FormatUtils.formatCurrency(summary.netCollection), Color.rgb(22, 163, 74))

        // Table Header
        currentY += cardHeight + 16f
        val startX = 20f
        val tableWidth = pageWidth - 40f
        val colWidths = floatArrayOf(58f, 44f, 50f, 44f, 50f, 44f, 50f, 50f, 55f, 55f, 55f)
        val headers = arrayOf("Date", "Pet(V)", "Pet(₹)", "Die(V)", "Die(₹)", "Trk(V)", "Trk(₹)", "Tot(V)", "Tot Coll", "Expense", "Net Coll")

        // Draw Table Header Background
        paint.color = Color.rgb(241, 245, 249)
        paint.style = Paint.Style.FILL
        canvas.drawRect(startX, currentY, startX + tableWidth, currentY + 20f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 7.5f
        paint.isFakeBoldText = true

        var xOffset = startX
        for (i in headers.indices) {
            paint.textAlign = if (i == 0) Paint.Align.LEFT else Paint.Align.RIGHT
            val textX = if (i == 0) xOffset + 4f else xOffset + colWidths[i] - 3f
            canvas.drawText(headers[i], textX, currentY + 13f, paint)
            xOffset += colWidths[i]
        }

        currentY += 20f
        // Draw Table Rows (up to 20 rows to fit page cleanly)
        paint.textSize = 7f
        paint.isFakeBoldText = false

        val displayRows = rows.take(22)
        for (rowIdx in displayRows.indices) {
            val r = displayRows[rowIdx]
            val rowY = currentY + (rowIdx * 17f)

            // Alternate row background
            if (rowIdx % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                paint.style = Paint.Style.FILL
                canvas.drawRect(startX, rowY, startX + tableWidth, rowY + 17f, paint)
            }

            // Row grid line
            paint.color = Color.rgb(241, 245, 249)
            paint.strokeWidth = 0.5f
            canvas.drawLine(startX, rowY + 17f, startX + tableWidth, rowY + 17f, paint)

            paint.color = Color.rgb(15, 23, 42)
            var cellX = startX
            val values = arrayOf(
                r.date,
                r.petrolCount.toString(),
                FormatUtils.formatCurrency(r.petrolAmount),
                r.dieselCount.toString(),
                FormatUtils.formatCurrency(r.dieselAmount),
                r.truckCount.toString(),
                FormatUtils.formatCurrency(r.truckAmount),
                r.totalVehicles.toString(),
                FormatUtils.formatCurrency(r.totalCollection),
                FormatUtils.formatCurrency(r.expenditure),
                FormatUtils.formatCurrency(r.netCollection)
            )

            for (i in values.indices) {
                paint.textAlign = if (i == 0) Paint.Align.LEFT else Paint.Align.RIGHT
                val textX = if (i == 0) cellX + 4f else cellX + colWidths[i] - 3f

                // Special color for net collection
                if (i == 10) {
                    paint.color = if (r.netCollection >= 0) Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
                    paint.isFakeBoldText = true
                } else if (i == 8) {
                    paint.color = Color.rgb(15, 107, 79)
                    paint.isFakeBoldText = true
                } else {
                    paint.color = Color.rgb(15, 23, 42)
                    paint.isFakeBoldText = false
                }

                canvas.drawText(values[i], textX, rowY + 11.5f, paint)
                cellX += colWidths[i]
            }
        }

        currentY += (displayRows.size * 17f) + 4f

        // Table Total Row
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.FILL
        canvas.drawRect(startX, currentY, startX + tableWidth, currentY + 19f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 7.5f
        paint.isFakeBoldText = true

        val totalValues = arrayOf(
            "TOTAL",
            summary.totalPetrolVehicles.toString(),
            FormatUtils.formatCurrency(summary.totalPetrolCollection),
            summary.totalDieselVehicles.toString(),
            FormatUtils.formatCurrency(summary.totalDieselCollection),
            summary.totalTruckVehicles.toString(),
            FormatUtils.formatCurrency(summary.totalTruckCollection),
            summary.totalVehicles.toString(),
            FormatUtils.formatCurrency(summary.grandTotalCollection),
            FormatUtils.formatCurrency(summary.totalOthersExpenditure),
            FormatUtils.formatCurrency(summary.netCollection)
        )

        var totX = startX
        for (i in totalValues.indices) {
            paint.textAlign = if (i == 0) Paint.Align.LEFT else Paint.Align.RIGHT
            val textX = if (i == 0) totX + 4f else totX + colWidths[i] - 3f
            if (i == 10) paint.color = Color.rgb(22, 163, 74) else paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(totalValues[i], textX, currentY + 13f, paint)
            totX += colWidths[i]
        }

        currentY += 28f

        // Optional: Itemized Expenditure Table (if present)
        if (expenditures.isNotEmpty() && currentY < 700f) {
            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("ITEMIZED OTHERS EXPENDITURES", startX, currentY, paint)
            currentY += 12f

            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(startX, currentY, startX + tableWidth, currentY + 16f, paint)
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 7.5f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Reason / Description", startX + 6f, currentY + 11f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Amount (₹)", startX + tableWidth - 6f, currentY + 11f, paint)

            currentY += 16f
            paint.isFakeBoldText = false
            for (exp in expenditures.take(6)) {
                paint.color = Color.rgb(30, 41, 59)
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("${exp.description} (${exp.category})", startX + 6f, currentY + 11f, paint)
                paint.textAlign = Paint.Align.RIGHT
                paint.color = Color.rgb(220, 38, 38)
                canvas.drawText(FormatUtils.formatCurrency(exp.amount), startX + tableWidth - 6f, currentY + 11f, paint)
                currentY += 14f
            }
        }

        // Bottom Signature and Verification Area
        val footerY = pageHeight - 65f
        paint.color = Color.rgb(148, 163, 184)
        paint.strokeWidth = 0.8f
        canvas.drawLine(startX, footerY, startX + 160f, footerY, paint)
        canvas.drawLine(pageWidth - startX - 160f, footerY, pageWidth - startX, footerY, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Prepared by Operator", startX + 80f, footerY + 12f, paint)
        canvas.drawText("Authorized Signatory & Stamp", pageWidth - startX - 80f, footerY + 12f, paint)

        // Footer note
        paint.textSize = 7f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Dash Pollution Testing Units • Official Record • Palas, Junagarh, Kalahandi, Odisha", pageWidth / 2f, pageHeight - 18f, paint)

        pdfDocument.finishPage(page)

        // Save file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeName = reportTitle.lowercase().replace(" ", "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(reportsDir, "dash_puc_${safeName}_$timestamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return pdfFile
    }

    private fun drawKpiCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        accentColor: Int
    ) {
        val paint = Paint()
        paint.isAntiAlias = true

        // Card bg
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 4f, 4f, paint)

        // Border
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 4f, 4f, paint)

        // Top accent line
        paint.color = accentColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(x, y, x + w, y + 3f, paint)

        // Title
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 6.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, x + (w / 2f), y + 17f, paint)

        // Value
        paint.color = accentColor
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText(value, x + (w / 2f), y + 33f, paint)
    }
}
