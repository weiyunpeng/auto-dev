package cc.unitmesh.devti.sketch.ui

import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Color constants used throughout the AutoDev UI
 */
object AutoDevColors {
    // Status colors
    val COMPLETED_STATUS = JBColor(0x59A869, 0x59A869) // Green
    val FAILED_STATUS = JBColor(0xD94F4F, 0xD94F4F) // Red
    val IN_PROGRESS_STATUS = JBColor(0x3592C4, 0x3592C4) // Blue
    val TODO_STATUS = JBColor(0x808080, 0x808080) // Gray

    // Text colors
    val COMPLETED_TEXT = JBColor(0x808080, 0x999999)
    val FAILED_TEXT = JBColor(0xD94F4F, 0xFF6B68)
    val IN_PROGRESS_TEXT = JBColor(0x3592C4, 0x589DF6)
    
    val SEPARATOR_BORDER = JBColor(0xE5E5E5, 0x323232)
    val LINK_COLOR = JBColor(0x3366CC, 0x589DF6)
    
    // Loading panel colors
    object LoadingPanel {
        // Background colors
        val BACKGROUND = JBColor(Color(245, 247, 250), Color(30, 32, 40))
        val FOREGROUND = JBColor(Gray._50, Gray._220)
        val BORDER = JBColor(Color(200, 210, 230), Color(60, 65, 80))
        
        // Progress bar colors
        val PROGRESS_COLOR = JBColor(Color(59, 130, 246), Color(59, 130, 246))
        val PROGRESS_BACKGROUND = JBColor(Color(229, 231, 235), Color(55, 65, 81))
        
        // Gradient colors
        val GRADIENT_COLOR1 = JBColor(Color(240, 245, 255, 200), Color(30, 40, 70, 200))
        val GRADIENT_COLOR2 = JBColor(Color(245, 240, 255, 200), Color(40, 30, 70, 200))
    }
}
