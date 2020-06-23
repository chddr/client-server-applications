package frontend.swing_ui.utils

import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


@FunctionalInterface
interface SimpleDocumentListener : DocumentListener {
    fun update(e: DocumentEvent?)
    override fun insertUpdate(e: DocumentEvent?) {
        update(e)
    }

    override fun removeUpdate(e: DocumentEvent?) {
        update(e)
    }

    override fun changedUpdate(e: DocumentEvent?) {
        update(e)
    }

    companion object {
        fun JTextField.addChangeListener(eventHandler: (DocumentEvent) -> Unit) {
            document.addDocumentListener(object : SimpleDocumentListener {
                override fun update(e: DocumentEvent?) {
                    if (e != null) {
                        eventHandler(e)
                    }
                }
            })
        }
    }
}