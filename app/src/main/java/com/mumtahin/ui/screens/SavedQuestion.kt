package com.mumtahin.ui.screens

/** One word inside a শব্দার্থ / বাক্য তৈরি / বিপরীত শব্দ question. */
internal data class WordItem(
    val id: Long,
    val word: String
)

/**
 * A saved question of any supported type. Add a new data class here for
 * each new question type — SavedQuestionCard and SubjectScreen's `when`
 * blocks are the only other places that need a matching new branch.
 */
internal sealed class SavedQuestion {
    abstract val id: Long
    abstract val marks: String

    data class Poem(
        override val id: Long,
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
}
