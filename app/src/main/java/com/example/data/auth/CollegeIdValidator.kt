package com.example.data.auth

/**
 * Standardized College ID System for MyCampus.
 * Follows a fixed-length (9-character) alphanumeric format matching official college student ID cards.
 * Example: BD25BE016 (BD = Branch/Campus, 25 = Year/Batch, BE = Degree/Spec, 016 = Roll Sequence)
 */
object CollegeIdValidator {

    const val FIXED_LENGTH = 9

    // Matches standard fixed 9-character alphanumeric pattern without spaces (e.g. BD25BE016, ST25CS001, MY26BC042)
    private val COLLEGE_ID_PATTERN = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{3}$")
    private val GENERAL_ALPHANUMERIC_PATTERN = Regex("^[A-Z0-9]{9}$")

    /**
     * Normalizes input by trimming and converting to uppercase.
     */
    fun normalize(input: String?): String {
        return input?.trim()?.uppercase()?.replace("\\s+".toRegex(), "") ?: ""
    }

    /**
     * Validates if the given ID strictly conforms to the fixed 9-character alphanumeric structure.
     */
    fun isValidFormat(collegeId: String?): Boolean {
        val clean = normalize(collegeId)
        if (clean.length != FIXED_LENGTH) return false
        return GENERAL_ALPHANUMERIC_PATTERN.matches(clean)
    }

    /**
     * Generates a standardized 9-character College ID given student attributes.
     * e.g., prefix="BD", year=25, degree="BE", sequence=16 -> "BD25BE016"
     */
    fun formatCollegeId(prefix: String, year2Digits: Int, degreeCode: String, sequenceNumber: Int): String {
        val p = prefix.trim().take(2).uppercase().padEnd(2, 'C')
        val y = String.format("%02d", year2Digits % 100)
        val d = degreeCode.trim().take(2).uppercase().padEnd(2, 'S')
        val s = String.format("%03d", sequenceNumber % 1000)
        return "$p$y$d$s"
    }
}
