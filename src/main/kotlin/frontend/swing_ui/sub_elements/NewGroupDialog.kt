package frontend.swing_ui.sub_elements

import frontend.HttpClientLogic
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

@Suppress("DuplicatedCode") // a lot of UI code
class NewGroupDialog(parent: JFrame, private val client: HttpClientLogic) : JDialog(parent, "Create a new group", true) {

    private val nameInput = JTextField()
    private val descInput = JTextField()
    private val createButton = createCreateButton()

    init {

        add(createPanel())

        minimumSize = Dimension(150, 100)
        isResizable = false
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        pack()
        isVisible = true
    }

    private fun createPanel(): JPanel {
        val bord = 5 // border size
        return JPanel().apply {
            border = BorderFactory.createEmptyBorder(bord, bord, bord, bord)
            layout = GridLayout(3   , 1, bord, bord)

            add(JLabel("Name:"))
            add(nameInput)
            add(JLabel("Description:"))
            add(descInput)
            add(createButton)

        }
    }

    private fun createCreateButton() = JButton("Create")
            .apply {
                addActionListener {
                    try {
                        val desc = descInput.text.trim()
                        client.createGroup(
                                nameInput.text.trim(),
                                if (!desc.isBlank()) desc else null
                        )
                        dispose()
                    } catch (e: Exception) {
                        this@NewGroupDialog.showError(e)
                    }

                }
            }

}
