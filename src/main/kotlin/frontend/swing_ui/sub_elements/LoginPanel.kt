package frontend.swing_ui.sub_elements

import frontend.http.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.utils.UiUtils.al
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.Component
import javax.swing.*

class LoginPanel(private val client: HttpClientLogic, private val parent: ClientApp) : JPanel() {

    private val login = JTextField("admin", 20)
    private val password = JPasswordField("admin", 20)

    private val loginButton = createLoginButton(login, password)
    private val logoutButton = createLogoutButton()

    init {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Login").al())
            add(login.al())
            add(JLabel("Password").al())
            add(password.al())
            add(Box.createVerticalStrut(5))
            add(JPanel().apply {
                al()
                add(loginButton)
                add(logoutButton)
            })
        })

    }

    private fun createLogoutButton(): Component? {
        return JButton("Log out!").apply {
            addActionListener {
                client.logout()
                this@LoginPanel.parent.removeTabs()
                loginButton.isEnabled = true
                JOptionPane.showMessageDialog(this, "Successfully logged out!", "Success", JOptionPane.INFORMATION_MESSAGE)

            }
        }
    }

    private fun createLoginButton(login: JTextField, password: JPasswordField): JButton {
        return JButton("Log in!").apply {
            addActionListener {
                try {
                    val loginResponse = client.sendLogin(
                            login.text,
                            String(password.password)
                    )
                    JOptionPane.showMessageDialog(this, "Successfully logged in:\nYour role: ${loginResponse.role}", "Success", JOptionPane.INFORMATION_MESSAGE)
                    when (loginResponse.role) {
                        "admin" -> this@LoginPanel.parent.addAdminTabs()
                        "user" -> this@LoginPanel.parent.addUserTabs()
                    }
                    loginButton.isEnabled = false
                } catch (e: Exception) {
                    this@LoginPanel.showError(e)
                }
            }
        }
    }
}