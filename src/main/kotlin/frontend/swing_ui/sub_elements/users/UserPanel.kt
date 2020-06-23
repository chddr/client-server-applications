package frontend.swing_ui.sub_elements.users

import db.entities.User
import frontend.http.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.utils.UiUtils.al
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.JOptionPane.*
import javax.swing.table.AbstractTableModel

@Suppress("DuplicatedCode")
class UserPanel(private val client: HttpClientLogic, private val parent: ClientApp) : JPanel() {

    companion object {
        private val colNames = arrayOf("Id", "Login", "Password", "Role")
    }

    private val addGroup = createAddUserButton()
    private var table = createTable()

    /*Info*/
    private var users = ArrayList<User>()

    init {
        layout = BorderLayout(5, 5)
        add(createQueryPanel(), BorderLayout.WEST)
        add(JScrollPane(table), BorderLayout.CENTER)
    }

    private fun refreshTable() {
        try {
            users = client.loadUsers()
            table.updateUI()
        } catch (e: Exception) {
            showError(e)
        }
    }

    private fun createQueryPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(addGroup.al())
            add(Box.createVerticalGlue())
            add(JButton("Refresh").apply {
                al()
                addActionListener { refreshTable() }
            })
        }
    }

    private fun createAddUserButton(): JButton {
        return JButton("Create new user").apply {
            background = Color.GREEN.darker()
            foreground = Color.WHITE
            addActionListener {
                try {
                    NewUserDialog(this@UserPanel.parent, client)
                    refreshTable()
                } catch (e: Exception) {
                    this@UserPanel.showError(e)
                }
            }
        }
    }

    private fun createTable(): JTable {
        val table = JTable(object : AbstractTableModel() {
            override fun getRowCount() = users.size
            override fun getColumnCount() = 4
            override fun getColumnName(i: Int) = colNames[i]

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
                return when (columnIndex) {
                    0 -> users[rowIndex].id
                    1 -> users[rowIndex].login
                    2 -> users[rowIndex].password
                    3 -> users[rowIndex].role
                    else -> null
                }
            }
        })
        table.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val t = mouseEvent.source as JTable
                if (mouseEvent.clickCount == 2 && t.selectedRow != -1) {
                    val row = t.selectedRow
                    try {
                        val option = showConfirmDialog(this@UserPanel,
                                "Are you sure you want to delete this user?",
                                "Confirm", YES_NO_OPTION, WARNING_MESSAGE)
                        if (option == YES_OPTION) {
                            client.deleteUser(t.getValueAt(row, 0) as Int)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        this@UserPanel.showError(java.lang.Exception("No such user. List seems to be outdated."))
                    }
                    refreshTable()
                }
            }
        })
        return table
    }
}

