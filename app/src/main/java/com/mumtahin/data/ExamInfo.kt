package com.mumtahin.data

import kotlinx.serialization.Serializable

/** Which name shows first on the A4 preview page header. */
@Serializable
internal enum class HeaderOrder {
    EXAM_FIRST,     // পরীক্ষার নাম আগে
    MADRASA_FIRST   // মাদ্রাসার নাম আগে
}

/**
 * All the fields shown/edited in ExamInfoSection — hoisted so Preview can
 * read them too, and persisted to disk via SubjectRepository, so this
 * must stay a plain `@Serializable` data class (no Composable state).
 */
@Serializable
internal data class ExamInfo(
    val examName: String = "",
    val madrasaName: String = "",
    val subject: String = "",
    val className: String = "",
    val duration: String = "",
    val fullMarks: String = "",
    val headerOrder: HeaderOrder = HeaderOrder.EXAM_FIRST
)
