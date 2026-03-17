package com.example.protocoltracker.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.protocoltracker.data.local.entity.DailyStepsEntry
import com.example.protocoltracker.data.local.entity.FoodDrinkEntry
import com.example.protocoltracker.data.local.entity.MilestoneEntry
import com.example.protocoltracker.data.local.entity.WaistEntry
import com.example.protocoltracker.data.local.entity.WeightEntry
import com.example.protocoltracker.data.local.entity.WorkoutEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ExportZip(
    val fileName: String,
    val uri: Uri
)

object ExportAllManager {

    suspend fun createExportZip(
        context: Context,
        foodEntries: List<FoodDrinkEntry>,
        workoutEntries: List<WorkoutEntry>,
        weightEntries: List<WeightEntry>,
        waistEntries: List<WaistEntry>,
        stepsEntries: List<DailyStepsEntry>,
        milestones: List<MilestoneEntry>
    ): ExportZip = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply {
            mkdirs()
        }

        exportDir.listFiles()?.forEach { it.delete() }

        val fileName = "protocol_tracker_export_${LocalDate.now()}.zip"
        val zipFile = File(exportDir, fileName)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zip ->
            writeCsvEntry(zip, "food_drinks.csv", foodDrinksCsv(foodEntries))
            writeCsvEntry(zip, "workouts.csv", workoutsCsv(workoutEntries))
            writeCsvEntry(zip, "weights.csv", weightsCsv(weightEntries))
            writeCsvEntry(zip, "waist.csv", waistCsv(waistEntries))
            writeCsvEntry(zip, "steps.csv", stepsCsv(stepsEntries))
            writeCsvEntry(zip, "milestones.csv", milestonesCsv(milestones))
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.file_provider",
            zipFile
        )

        ExportZip(
            fileName = fileName,
            uri = uri
        )
    }

    fun shareZip(
        context: Context,
        exportZip: ExportZip
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, exportZip.uri)
            putExtra(Intent.EXTRA_SUBJECT, exportZip.fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Export all")
        )
    }

    private fun writeCsvEntry(
        zip: ZipOutputStream,
        entryName: String,
        content: String
    ) {
        zip.putNextEntry(ZipEntry(entryName))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun csvCell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun foodDrinksCsv(entries: List<FoodDrinkEntry>): String {
        val header = "id,date,time,type,name,calories,protein_grams,template_id"
        val rows = entries.map {
            listOf(
                it.id.toString(),
                csvCell(it.entryDate),
                csvCell(it.timeSlot),
                csvCell(it.entryType.name),
                csvCell(it.name),
                it.calories.toString(),
                it.proteinGrams?.toString() ?: "",
                it.templateId?.toString() ?: ""
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun workoutsCsv(entries: List<WorkoutEntry>): String {
        val header = "id,date,type,intensity,minutes"
        val rows = entries.map {
            listOf(
                it.id.toString(),
                csvCell(it.entryDate),
                csvCell(it.workoutType.name),
                csvCell(it.intensity.name),
                it.minutes.toString()
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun weightsCsv(entries: List<WeightEntry>): String {
        val header = "id,date,time,weight_kg"
        val rows = entries.map {
            listOf(
                it.id.toString(),
                csvCell(it.entryDate),
                csvCell(it.entryTime),
                it.weightKg.toString()
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun waistCsv(entries: List<WaistEntry>): String {
        val header = "id,date,waist_cm"
        val rows = entries.map {
            listOf(
                it.id.toString(),
                csvCell(it.entryDate),
                it.waistCm.toString()
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun stepsCsv(entries: List<DailyStepsEntry>): String {
        val header = "date,steps"
        val rows = entries.map {
            listOf(
                csvCell(it.entryDate),
                it.steps.toString()
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun milestonesCsv(entries: List<MilestoneEntry>): String {
        val header = "id,target_weight_kg,target_date,reward_text,sort_order"
        val rows = entries.map {
            listOf(
                it.id.toString(),
                it.targetWeightKg.toString(),
                csvCell(it.targetDate),
                csvCell(it.rewardText),
                it.sortOrder.toString()
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}