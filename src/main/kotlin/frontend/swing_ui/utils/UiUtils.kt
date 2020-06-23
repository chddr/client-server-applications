package frontend.swing_ui.utils

import java.awt.Component
import javax.swing.JComponent
import javax.swing.JOptionPane

object UiUtils {
    fun Component.showError(e: Exception) {
        JOptionPane.showMessageDialog(this, e.message, "Error", JOptionPane.ERROR_MESSAGE)
    }

    fun JComponent.al(): JComponent {
        alignmentX = Component.LEFT_ALIGNMENT
        return this
    }
}