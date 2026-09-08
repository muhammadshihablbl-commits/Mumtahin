package com.mumtahin.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mumtahin.R

/**
 * অ্যাপের একমাত্র/ডিফল্ট ফন্ট — Kalpurush (বাংলা ও ইংরেজি দুটোতেই), তিনটা
 * ওজনসহ। res/font/-এ ফাইলগুলো ঠিক এই নামে থাকতে হবে:
 * kalpurush_regular.ttf, kalpurush_medium.ttf, kalpurush_bold.ttf
 * (তোমার ডাউনলোড করা ফাইলের নাম অন্যরকম হলে রিনেম করে বসাও)
 */
val KalpurushFontFamily = FontFamily(
    Font(R.font.kalpurush_regular, FontWeight.Normal),
    Font(R.font.kalpurush_medium, FontWeight.Medium),
    Font(R.font.kalpurush_bold, FontWeight.Bold)
)
