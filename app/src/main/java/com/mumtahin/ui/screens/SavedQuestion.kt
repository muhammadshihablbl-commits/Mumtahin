package com.mumtahin.ui.screens

/** One word inside a শব্দার্থ / বাক্য তৈরি / বিপরীত শব্দ question. */
internal data class WordItem(
    val id: Long,
    val word: String
)

/** Layout style for a set of গাণিতিক সমস্যা math problems. */
internal enum class MathLayout {
    VERTICAL,   // উপর-নিচে (কলাম ফর্ম)
    HORIZONTAL  // পাশাপাশি (ইনলাইন)
}

/** One "operand1 operator operand2" math problem. */
internal data class MathProblemEntry(
    val operand1: String,
    val operator: String, // "+", "−", "×", "÷"
    val operand2: String
)

/** বাংলা ক)/খ)/গ)... ordinal labels used by any sub-question list. */
internal val banglaOrdinalLabels = listOf(
    "ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ",
    "ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন",
    "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ",
    "স", "হ"
)

internal fun ordinalLabel(index: Int): String =
    banglaOrdinalLabels.getOrNull(index) ?: (index + 1).toString()

/**
 * A saved question of any supported type. Add a new data class here for
 * each new question type — SavedQuestionCard and SubjectScreen's `when`
 * blocks are the only other places that need a matching new branch.
 */
internal sealed class SavedQuestion {
    abstract val id: Long
    abstract val marks: String

    /** One question + a single (usually long) answer — কবিতা and প্রশ্ন share this. */
    data class SingleQuestion(
        override val id: Long,
        val typeTitle: String,
        val questionText: String,
        override val marks: String
    ) : SavedQuestion()

    data class WordList(
        override val id: Long,
        val typeTitle: String,
        val questionText: String,
        val words: List<WordItem>,
        override val marks: String
    ) : SavedQuestion()

    data class FillBlanks(
        override val id: Long,
        val questionText: String,
        val subQuestions: List<String>,
        override val marks: String
    ) : SavedQuestion()

    /** A main question plus a growing list of ক)/খ)... short-answer sub-questions. */
    data class ShortQuestions(
        override val id: Long,
        val questionText: String,
        val subQuestions: List<String>,
        override val marks: String
    ) : SavedQuestion()
    
    data class TrueFalse(
        override val id: Long,
        val questionText: String,
        val statements: List<String>,
        override val marks: String
    ) : SavedQuestion()
    
    data class MathProblem(
        override val id: Long,
        val questionText: String,
        val layout: MathLayout,
        val problems: List<MathProblemEntry>,
        override val marks: String
    ) : SavedQuestion()
}
