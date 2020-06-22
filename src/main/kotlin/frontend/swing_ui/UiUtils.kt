package frontend.swing_ui

import java.awt.Component
import javax.swing.JOptionPane

object UiUtils {
    fun Component.showError(e: Exception) {
        JOptionPane.showMessageDialog(this, e.message, "Error", JOptionPane.ERROR_MESSAGE)
    }
}