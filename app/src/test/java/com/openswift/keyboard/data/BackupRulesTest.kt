package com.openswift.keyboard.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRulesTest {

    @Test
    fun manifestDisablesBackupAndReferencesBothRuleFormats() {
        val manifest = source("AndroidManifest.xml")

        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:fullBackupContent=\"@xml/backup_rules\""))
        assertTrue(manifest.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\""))
    }

    @Test
    fun backupRulesExcludeSharedPreferencesFromBackupAndTransfer() {
        val legacyRules = source("res/xml/backup_rules.xml")
        val extractionRules = source("res/xml/data_extraction_rules.xml")

        assertTrue(legacyRules.contains("<exclude domain=\"sharedpref\" path=\".\""))
        assertTrue(extractionRules.contains("<cloud-backup>"))
        assertTrue(extractionRules.contains("<device-transfer>"))
        assertEquals(2, extractionRules.split("domain=\"sharedpref\"").size - 1)
    }

    private fun source(relativePath: String): String {
        val file = File("src/main/$relativePath")
        check(file.isFile) { "Missing source file: ${file.absolutePath}" }
        return file.readText()
    }
}
