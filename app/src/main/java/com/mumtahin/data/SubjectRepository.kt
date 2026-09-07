package com.mumtahin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mumtahin.data.ExamInfo
import com.mumtahin.data.SavedQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.subjectDataStore: DataStore<Preferences> by preferencesDataStore(name = "subject_data")

/**
 * Everything persisted for one subject (বাংলা/ইংরেজি/অংক/আরবী) — exam
 * info + the saved question list + whether the exam-info card was left
 * collapsed. Serialized as a single JSON string per subject inside
 * DataStore Preferences: simple key-value, no schema/migrations to
 * manage. If the app later needs cross-subject search or complex
 * queries, Room would be worth revisiting — not needed for this size
 * of data yet.
 */
@Serializable
internal data class SubjectData(
    val examInfo: ExamInfo = ExamInfo(),
    val examInfoExpanded: Boolean = true,
    val savedQuestions: List<SavedQuestion> = emptyList()
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Reads/writes [SubjectData] per subject name. Pass the *application*
 * context in (not an Activity context) so this can safely outlive any
 * one screen — SubjectViewModel holds one of these for its lifetime.
 */
internal class SubjectRepository(private val appContext: Context) {

    private fun keyFor(subjectName: String) = stringPreferencesKey("subject_$subjectName")

    /** Emits the saved data for this subject on every change; null if nothing saved yet. */
    fun observe(subjectName: String): Flow<SubjectData?> =
        appContext.subjectDataStore.data.map { prefs ->
            prefs[keyFor(subjectName)]?.let { raw ->
                // A corrupt or outdated-schema entry should never crash the
                // screen — worst case we fall back to "nothing saved yet".
                runCatching { json.decodeFromString(SubjectData.serializer(), raw) }.getOrNull()
            }
        }

    suspend fun save(subjectName: String, data: SubjectData) {
        appContext.subjectDataStore.edit { prefs ->
            prefs[keyFor(subjectName)] = json.encodeToString(SubjectData.serializer(), data)
        }
    }
}
