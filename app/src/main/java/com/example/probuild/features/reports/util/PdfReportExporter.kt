package com.example.probuild.features.reports.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.probuild.features.reports.domain.model.ProjectReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportExporter {

    suspend fun generateAndSharePdf(context: Context, data: ProjectReportData): Result<File> = withContext(Dispatchers.IO) {
        try {
            val project = data.activeProject ?: return@withContext Result.failure(IllegalStateException("No active project selected"))

            val reportsDir = File(context.filesDir, "reports").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(reportsDir, "ProBuild_Report_${project.projectName.replace(" ", "_")}_$timestamp.pdf")

            val pdfDocument = PdfDocument()
            // Standard A4 Size: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val primaryPaint = Paint().apply {
                color = Color.parseColor("#30343B") // Graphite Gray
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 22f
                isAntiAlias = true
            }

            val accentPaint = Paint().apply {
                color = Color.parseColor("#849780") // Muted Sage
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 14f
                isAntiAlias = true
            }

            val bodyBoldPaint = Paint().apply {
                color = Color.parseColor("#30343B")
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 11f
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#30343B")
                typeface = Typeface.DEFAULT
                textSize = 10f
                isAntiAlias = true
            }

            val mutedPaint = Paint().apply {
                color = Color.parseColor("#858A90")
                typeface = Typeface.DEFAULT
                textSize = 9f
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#E7E8E3")
                strokeWidth = 1f
            }

            val boxPaint = Paint().apply {
                color = Color.parseColor("#F7F7F4")
                style = Paint.Style.FILL
            }

            var y = 40f
            val margin = 40f
            val rightMargin = 555f

            // 1. Header
            canvas.drawRect(margin, y, rightMargin, y + 60f, boxPaint)
            canvas.drawText("PROBUILD SITE REPORT", margin + 15f, y + 30f, primaryPaint)
            canvas.drawText("CIVIL ENGINEERING & PROJECT MANAGEMENT", margin + 15f, y + 48f, accentPaint)

            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generated: $dateStr", margin + 350f, y + 35f, mutedPaint)
            y += 80f

            // 2. Project Information Section
            canvas.drawText("PROJECT DETAILS", margin, y, accentPaint)
            y += 8f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
            y += 18f

            canvas.drawText("Project Name: ${project.projectName}", margin, y, bodyBoldPaint)
            canvas.drawText("Status: ${project.status.name}", margin + 300f, y, bodyBoldPaint)
            y += 16f
            canvas.drawText("Client: ${project.clientName.ifBlank { "N/A" }}", margin, y, bodyPaint)
            canvas.drawText("Stage: ${project.projectStage.name}", margin + 300f, y, bodyPaint)
            y += 16f
            canvas.drawText("Location: ${project.location.ifBlank { "N/A" }}", margin, y, bodyPaint)
            canvas.drawText("Progress: ${(project.progress * 100).toInt()}%", margin + 300f, y, bodyPaint)
            y += 25f

            // 3. Executive Metrics Summary Box
            canvas.drawRect(margin, y, rightMargin, y + 55f, boxPaint)
            var xBox = margin + 15f
            val colWidth = 120f

            canvas.drawText("TOTAL CALCS", xBox, y + 20f, mutedPaint)
            canvas.drawText("${data.totalCalculationsCount}", xBox, y + 40f, primaryPaint)

            xBox += colWidth
            canvas.drawText("DIARY LOGS", xBox, y + 20f, mutedPaint)
            canvas.drawText("${data.totalDiaryEntriesCount}", xBox, y + 40f, primaryPaint)

            xBox += colWidth
            canvas.drawText("TOTAL LABOUR", xBox, y + 20f, mutedPaint)
            canvas.drawText("${data.totalLabourRecorded} workers", xBox, y + 40f, primaryPaint)

            xBox += colWidth
            canvas.drawText("AVG LABOUR/DAY", xBox, y + 20f, mutedPaint)
            canvas.drawText("%.1f".format(data.averageLabourPerEntry), xBox, y + 40f, primaryPaint)

            y += 75f

            // 4. Engineering Quantity Totals
            canvas.drawText("ENGINEERING QUANTITY TOTALS", margin, y, accentPaint)
            y += 8f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
            y += 18f

            fun drawQuantityRow(label: String, valStr: String) {
                canvas.drawText(label, margin, y, bodyPaint)
                canvas.drawText(valStr, margin + 350f, y, bodyBoldPaint)
                y += 16f
            }

            drawQuantityRow("Concrete Volume:", "%.2f m³".format(data.totalConcreteVolume))
            drawQuantityRow("Steel Reinforcement Weight:", "%.2f kg".format(data.totalSteelWeight))
            drawQuantityRow("Brick Count:", "%.0f pcs".format(data.totalBricksCount))
            drawQuantityRow("Measured Surface Area:", "%.2f m²".format(data.totalAreaCalculated))
            drawQuantityRow("Bulk Volume:", "%.2f m³".format(data.totalVolumeCalculated))

            y += 15f

            // 5. Recent Calculations Breakdown
            if (data.recentCalculations.isNotEmpty()) {
                canvas.drawText("RECENT CALCULATIONS LOG", margin, y, accentPaint)
                y += 8f
                canvas.drawLine(margin, y, rightMargin, y, linePaint)
                y += 18f

                data.recentCalculations.take(4).forEach { calc ->
                    canvas.drawText(calc.displayName, margin, y, bodyBoldPaint)
                    canvas.drawText("${calc.resultVal} ${calc.resultUnit}", margin + 350f, y, bodyBoldPaint)
                    y += 16f
                }
                y += 15f
            }

            // 6. Recent Site Diary Activity
            if (data.recentDiaryEntries.isNotEmpty()) {
                canvas.drawText("RECENT SITE DIARY LOGS", margin, y, accentPaint)
                y += 8f
                canvas.drawLine(margin, y, rightMargin, y, linePaint)
                y += 18f

                val logDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                data.recentDiaryEntries.take(3).forEach { entry ->
                    val dateHeader = logDateFormat.format(Date(entry.date)) + " (${entry.labourCount} Workers)"
                    canvas.drawText(dateHeader, margin, y, bodyBoldPaint)
                    y += 14f
                    val workText = if (entry.workCompleted.length > 70) entry.workCompleted.take(67) + "..." else entry.workCompleted
                    canvas.drawText(workText, margin + 10f, y, bodyPaint)
                    y += 18f
                }
            }

            // 7. Footer
            canvas.drawLine(margin, 800f, rightMargin, 800f, linePaint)
            canvas.drawText("ProBuild Construction Management Suite • Page 1 of 1", margin, 818f, mutedPaint)

            pdfDocument.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ProBuild Report PDF"))
        } catch (e: Exception) {
            // Gracefully handle intent failures
        }
    }
}
