package com.mumtahin.ui.preview

import com.mumtahin.data.ExamInfo
import com.mumtahin.data.HeaderOrder
import com.mumtahin.data.MathLayout
import com.mumtahin.data.MathProblemEntry
import com.mumtahin.data.SavedQuestion
import com.mumtahin.data.ordinalLabel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mumtahin.R

/**
 * Opened by tapping the preview (eye) icon on SubjectScreen's toolbar.
 * Renders the exam info + saved questions inside a canvas locked to the
 * real A4 aspect ratio (210mm : 297mm), floating on a neutral backdrop —
 * like an actual sheet of paper. The pencil icon in this screen's toolbar
 * returns to SubjectScreen (Exam Information + প্রশ্নের ধরন); there's no
 * separate back arrow, since the pencil is the only way back by design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionPreviewScreen(
    subjectName: String,
    examInfo: ExamInfo,
    savedQuestions: List<SavedQuestion>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // UI-only demo state for the formatting bar — purely visual (which
    // button looks "active") until a real rich-text editor is wired in.
    // A future integration should replace these with the editor's actual
    // selection state instead of local remember.
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var currentAlignment by remember { mutableStateOf(RichTextAlign.LEFT) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("$subjectName — প্রিভিউ") },
                    navigationIcon = {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // UI-only rich-text formatting bar — see RichTextToolbar.kt
                // for the "plug a real editor in later" contract.
                RichTextToolbar(
                    isBoldActive = isBoldActive,
                    isItalicActive = isItalicActive,
                    currentAlignment = currentAlignment,
                    onBoldClick = { isBoldActive = !isBoldActive },
                    onItalicClick = { isItalicActive = !isItalicActive },
                    onAlignClick = { currentAlignment = it }
                    // onIncreaseFontSizeClick / onDecreaseFontSizeClick /
                    // onIncreaseIndentClick / onDecreaseIndentClick /
                    // onInsertTableClick left as no-ops for now — wire
                    // these up once there's real content to format.
                )
            }
        }
    ) { innerPadding ->
        // Neutral backdrop (like a document editor canvas) so the white
        // A4 page reads clearly as a distinct sheet of paper.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            A4PageCanvas(
                examInfo = examInfo,
                savedQuestions = savedQuestions,
                subjectName = subjectName
            )
        }
    }
}

/**
 * A page locked to the real A4 ratio (width:height = 210:297 = 1:√2) via
 * `aspectRatio`, so it stays proportionally accurate on any screen width.
 * Its own content scrolls internally if it overflows one page's height.
 */
@Composable
private fun A4PageCanvas(
    examInfo: ExamInfo,
    savedQuestions: List<SavedQuestion>,
    subjectName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(210f / 297f), // exact A4 portrait ratio
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp), // real paper has near-sharp corners
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp) // page margin — slightly tighter than before
        ) {
            PageHeader(examInfo = examInfo)

            // গ্যাপ ৪: exam info (উপরের অংশ) ও নিচের ডিভাইডার লাইনের মাঝের ফাঁক
            Spacer(modifier = Modifier.height(EXAM_INFO_LINE_GAP * 5))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.4f))
            // গ্যাপ ৫: ডিভাইডার ও প্রথম প্রশ্নের মাঝের ফাঁক
            Spacer(modifier = Modifier.height(EXAM_INFO_LINE_GAP * 5))

            if (savedQuestions.isEmpty()) {
                Text(
                    text = "এখনো কোনো প্রশ্ন যোগ করা হয়নি",
                    fontSize = 10.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            } else {
                savedQuestions.forEachIndexed { index, question ->
                    val numberLabel = formatQuestionNumber(index + 1, subjectName)
                    if (question is SavedQuestion.MathProblem) {
                        MathProblemPreviewRow(number = numberLabel, question = question)
                    } else {
                        PreviewQuestionRow(number = numberLabel, question = question)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * সবচেয়ে কাছাকাছি লাইন-স্পেসিং — Android ফন্টের built-in "font padding"
 * বন্ধ করে দেয় (`includeFontPadding = false`), যেটা শুধু `lineHeight`
 * সেট করলেও পুরোপুরি যায় না। exam info-র নাম/মেটা লাইনগুলোতে এই
 * `style` + `lineHeight = fontSize` — দুটো একসাথে দিলে glyph-এর
 * চারপাশে প্রায় কোনো বাড়তি ফাঁক থাকবে না।
 */
private val tightLineHeightTextStyle = TextStyle(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    ),
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

/**
 * Exam info (উপরের অংশ)-এর সবগুলো লাইন-গ্যাপ এই একটা মান থেকেই নেয় —
 * এই একটা নম্বর বদলালে ৫টা গ্যাপই (নাম-নাম, নাম-বিষয়, বিষয়-সময়,
 * header-ডিভাইডার, ডিভাইডার-প্রশ্ন) একসাথে সমান হারে কম-বেশি হবে।
 */
private val EXAM_INFO_LINE_GAP = 0.dp

@Composable
private fun PageHeader(examInfo: ExamInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val nameOrder = if (examInfo.headerOrder == HeaderOrder.MADRASA_FIRST) {
            listOf(examInfo.madrasaName to 13.sp, examInfo.examName to 11.sp)
        } else {
            listOf(examInfo.examName to 13.sp, examInfo.madrasaName to 11.sp)
        }

        nameOrder.forEach { (name, size) ->
            if (name.isNotBlank()) {
                Text(
                    text = name,
                    style = tightLineHeightTextStyle,
                    fontSize = size,
                    lineHeight = size, // ফন্টের ডিফল্ট লাইন-হাইট বাদ, fontSize-এর সমান — সবচেয়ে কাছাকাছি স্পেসিং
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        // গ্যাপ ১: পরীক্ষার নাম ও মাদ্রাসার নামের মাঝের ফাঁক
                        .padding(bottom = EXAM_INFO_LINE_GAP)
                )
            }
        }

        // গ্যাপ ২: নাম দুটোর নিচে, বিষয়/শ্রেণী সারির উপরে ফাঁক
        Spacer(modifier = Modifier.height(EXAM_INFO_LINE_GAP))

        // বিষয় ও শ্রেণী — কাছাকাছি, পুরো জোড়াটা পাতার মাঝখানে
        if (examInfo.subject.isNotBlank() || examInfo.className.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally)
            ) {
                MetaLine(label = "বিষয়", value = examInfo.subject)
                MetaLine(label = "শ্রেণী", value = examInfo.className)
            }
        }

        // সময় ও পূর্ণমান — একই সারিতে পাশাপাশি
        if (examInfo.duration.isNotBlank() || examInfo.fullMarks.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // গ্যাপ ৩: বিষয়/শ্রেণী সারি ও সময়/পূর্ণমান সারির মাঝের ফাঁক
                    .padding(top = EXAM_INFO_LINE_GAP),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaLine(label = "সময়", value = examInfo.duration)
                MetaLine(label = "পূর্ণমান", value = examInfo.fullMarks)
            }
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    if (value.isBlank()) {
        Spacer(modifier = Modifier.width(1.dp))
        return
    }
    Text(
        text = "$label: $value",
        style = tightLineHeightTextStyle,
        fontSize = 9.sp,
        lineHeight = 9.sp, // ফন্টের ডিফল্ট লাইন-হাইট বাদ দিয়ে fontSize-এর সমান করে সবচেয়ে কাছাকাছি
        color = Color.Black
    )
}

@Composable
private fun PreviewQuestionRow(number: String, question: SavedQuestion) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$number. ${previewQuestionText(question)}",
                style = tightLineHeightTextStyle,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = question.marks,
                style = tightLineHeightTextStyle,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (question is SavedQuestion.WordList) {
            // শব্দার্থ/বাক্য তৈরি/বিপরীত শব্দ — কমা দিয়ে আলাদা করা।
            val wordsLine = question.words.map { it.word }.filter { it.isNotBlank() }.joinToString(", ")
            if (wordsLine.isNotBlank()) {
                Text(
                    text = wordsLine,
                    style = tightLineHeightTextStyle,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = Color.Black.copy(alpha = 0.85f),
                    // এই top প্যাডিং কমালে/বাড়ালে মূল প্রশ্ন ও শব্দ-তালিকার মাঝের ফাঁক বদলাবে
                    modifier = Modifier.padding(top = 2.dp, start = 14.dp)
                )
            }
        } else {
            previewSubItems(question)?.let { items ->
                Column(modifier = Modifier.padding(top = 2.dp, start = 14.dp)) {
                    items.forEachIndexed { i, text ->
                        Text(
                            text = "${ordinalLabel(i)}) $text",
                            style = tightLineHeightTextStyle,
                            fontSize = 9.sp,
                            lineHeight = 9.sp,
                            color = Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun previewQuestionText(question: SavedQuestion): String = when (question) {
    is SavedQuestion.SingleQuestion -> question.questionText
    is SavedQuestion.WordList -> question.questionText
    is SavedQuestion.FillBlanks -> question.questionText
    is SavedQuestion.ShortQuestions -> question.questionText
    is SavedQuestion.TrueFalse -> question.questionText
    is SavedQuestion.MathProblem -> question.questionText
}

/** Sub-list to render under the question text, if the type has one (WordList/MathProblem render separately). */
private fun previewSubItems(question: SavedQuestion): List<String>? = when (question) {
    is SavedQuestion.WordList -> null // rendered separately, comma-joined, above
    is SavedQuestion.FillBlanks -> question.subQuestions
    is SavedQuestion.ShortQuestions -> question.subQuestions
    is SavedQuestion.TrueFalse -> question.statements
    is SavedQuestion.MathProblem -> null // rendered separately by MathProblemPreviewRow
    is SavedQuestion.SingleQuestion -> null
}

/**
 * গাণিতিক সমস্যা renders as a 2-per-row grid of either column-form
 * (উপর-নিচে) or inline (পাশাপাশি) problems, based on `question.layout`.
 */
@Composable
private fun MathProblemPreviewRow(number: String, question: SavedQuestion.MathProblem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$number. ${question.questionText}",
                style = tightLineHeightTextStyle,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = question.marks,
                style = tightLineHeightTextStyle,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        question.problems.chunked(2).forEach { rowEntries ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                rowEntries.forEach { entry ->
                    if (question.layout == MathLayout.VERTICAL) {
                        VerticalMathEntry(entry)
                    } else {
                        HorizontalMathEntry(entry)
                    }
                }
            }
        }
    }
}

/** Traditional column form: operand1 on top, operator+operand2 below, line, then blank space for the answer. */
@Composable
private fun VerticalMathEntry(entry: MathProblemEntry) {
    Column(horizontalAlignment = Alignment.End) {
        Text(entry.operand1, fontSize = 11.sp, color = Color.Black)
        Row {
            Text(
                "${entry.operator} ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(entry.operand2, fontSize = 11.sp, color = Color.Black)
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 3.dp).width(44.dp),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(18.dp)) // answer space
    }
}

/** Inline form: "operand1 operator operand2 = ____". */
@Composable
private fun HorizontalMathEntry(entry: MathProblemEntry) {
    Text(
        text = "${entry.operand1} ${entry.operator} ${entry.operand2} = _______",
        fontSize = 11.sp,
        color = Color.Black
    )
}

private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

/** "1, 2, 3..." for other subjects; "১, ২, ৩..." for বাংলা. */
private fun formatQuestionNumber(number: Int, subjectName: String): String {
    if (subjectName != "বাংলা") return number.toString()
    return number.toString().map { ch -> if (ch.isDigit()) banglaDigits[ch - '0'] else ch }.joinToString("")
}
