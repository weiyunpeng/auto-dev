package cc.unitmesh.terminal

import java.text.SimpleDateFormat
import java.util.*

data class ShellSuggestContext(
    val question: String,
    val shellPath: String,
    val cwd: String,
    // today's date like 20240322
    val date: String = SimpleDateFormat("yyyyMMdd").format(Date()),
    // operating system name
    val os: String = System.getProperty("os.name")
)