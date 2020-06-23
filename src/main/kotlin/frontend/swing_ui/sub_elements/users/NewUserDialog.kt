package frontend.swing_ui.sub_elements.users

import db.entities.User
import frontend.http.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

@Suppress("DuplicatedCode") // a lot of UI code
class NewUserDialog(parent: ClientApp, private var client: HttpClientLogic) : JDialog(parent, "Create a new user", true) {

    private val loginInput = JTextField()
    private val passwordInput = JTextField()
    private val roleInput = JTextField()
    private val createButton = createCreateButton()

    private val labels = listOf("Login:", "Password:", "Role:").map { JLabel(it) }

    init {
        add(createPanel())

        minimumSize = Dimension(200, 150)
        isResizable = false
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        pack()
        isVisible = true
    }


    private fun formUser(): User {
        return User(loginInput.text.trim(),
                passwordInput.text.trim(),
                roleInput.text.trim())

    }

    private fun createPanel(): JPanel {
        val bord = 5 // border size
        return JPanel().apply {
            border = BorderFactory.createEmptyBorder(bord, bord, bord, bord)
            layout = GridLayout(4, 1, bord, bord)

            add(labels[0])
            add(loginInput)
            add(labels[1])
            add(passwordInput)
            add(labels[2])
            add(roleInput)
            add(createButton)
        }
    }

    private fun createCreateButton() = JButton("Create")
            .apply {
                addActionListener {
                    try {
                        client.createUser(formUser())
                        dispose()
                    } catch (e: Exception) {
                        this@NewUserDialog.showError(e)
                    }

                }
            }


}
