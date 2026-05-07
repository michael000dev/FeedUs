package com.seazon.feedus.platform

import platform.Foundation.NSLocale
import platform.Foundation.languageCode
import platform.Foundation.currentLocale

actual fun getSystemLanguage(): String = NSLocale.currentLocale.languageCode
