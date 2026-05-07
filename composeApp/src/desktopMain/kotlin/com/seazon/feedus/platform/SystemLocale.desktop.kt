package com.seazon.feedus.platform

import java.util.Locale

actual fun getSystemLanguage(): String = Locale.getDefault().language
