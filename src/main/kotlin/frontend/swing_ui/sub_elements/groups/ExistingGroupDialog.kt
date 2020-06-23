package frontend.swing_ui.sub_elements.groups

import db.entities.Group
import frontend.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.utils.SimpleDocumentListener.Companion.addChangeListener
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class ExistingGroupDialog(parent: ClientApp, private val client: HttpClientLogic, id: Int) : JDialog(parent, "Group", true) {
    private val group = client.loadGroup(id)

    private val idInput = createIdInput()
    private val nameInput = createNameInput()
    private val descInput = createDescInput()

    private val submitButton = createSubmitButton()
    private val deleteButton = createDeleteButton()

    init {
        add(createPanel())

        minimumSize = Dimension(150, 100)
        isResizable = false
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        changeRegistered()
        pack()
        isVisible = true
    }

    private fun changeRegistered() {
        submitButton.isEnabled = (group.name != name() || group.description != desc())
    }

    private fun createPanel(): JPanel {
        val bord = 5 // border size
        return JPanel().apply {
            border = BorderFactory.createEmptyBorder(bord, bord, bord, bord)
            layout = GridLayout(3, 2, bord, bord)

            add(JLabel("Id:"))
            add(idInput)
            add(JLabel("Name:"))
            add(nameInput)
            add(JLabel("Description:"))
            add(descInput)
            add(submitButton)
            add(deleteButton)
        }
    }

    /*
Very big
ugly
declarative
methods
 */

    private fun createIdInput() = JTextField(group.id.toString())
            .apply {
                isEditable = false
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createNameInput() = JTextField(group.name)
            .apply {
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createDescInput() = JTextField(group.description)
            .apply {
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createSubmitButton() = JButton("Submit change")
            .apply {
                isEnabled = false
                addActionListener {
                    try {
                        client.modifyGroup(Group(group.id, name(), desc()))
                        dispose()
                    } catch (e: Exception) {
                        this@ExistingGroupDialog.showError(e)
                    }

                }
            }

    private fun name() = nameInput.text.trim()
    private fun desc() = descInput.text.trim()

    private fun createDeleteButton() = JButton("Delete product")
            .apply {
                background = Color.RED.darker()
                foreground = Color.BLACK.brighter()
                addActionListener {
                    val response = JOptionPane.showConfirmDialog(this@ExistingGroupDialog, "Do you really want to delete the group?\n" +
                            "Along with it all associated products will be removed.", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                    if (response == JOptionPane.NO_OPTION) return@addActionListener
                    try {
                        client.deleteGroup(group.id)
                        dispose()
                    } catch (e: Exception) {
                        this@ExistingGroupDialog.showError(e)
                    }

                }
            }


}
