package frontend.swing_ui.sub_elements.groups

import db.entities.Group
import frontend.http.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.utils.UiUtils.al
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel

@Suppress("DuplicatedCode")
class GroupsPanel(private val client: HttpClientLogic, private val parent: ClientApp) : JPanel() {

    companion object {
        private val colNames = arrayOf("Id", "Name", "Description")
    }

    private val addGroup = createAddGroupButton()
    private val queryInput = JTextField(10)

    private var table = createTable()

    /*Info*/
    private var groups = ArrayList<Group>()
    private var query: String? = null

    init {
        layout = BorderLayout(5, 5)
        add(createQueryPanel(), BorderLayout.WEST)
        add(JScrollPane(table), BorderLayout.CENTER)
    }

    private fun refreshTable() {
        try {
            groups = client.loadGroups(query)
            table.updateUI()
        } catch (e: Exception) {
            showError(e)
        }
    }

    /*
        * Bulky,
        * Ugly,
        * and Massive create methods.
        * Extracting them to a separate classes would be overkill
        * So I decided to keep'em here where I can reach
        * for all the interconnected variables more easily
        */

    private fun createQueryPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(addGroup.al())
            add(JLabel("Query:").al())
            add(queryInput.al().apply {
                maximumSize = Dimension(200, 50)
            })

            add(Box.createVerticalGlue())

            add(JButton("Refresh").apply {
                al()
                addActionListener {
                    query = queryInput.text.trim()
                    refreshTable()
                }
            })
        }
    }

    private fun createAddGroupButton(): JButton {
        return JButton("Create new group").apply {
            background = Color.GREEN.darker()
            foreground = Color.WHITE
            addActionListener {
                try {
                    NewGroupDialog(this@GroupsPanel.parent, client)
                    refreshTable()
                } catch (e: Exception) {
                    this@GroupsPanel.showError(e)
                }
            }
        }
    }

    private fun createTable(): JTable {
        return JTable(object : AbstractTableModel() {
            override fun getRowCount() = groups.size
            override fun getColumnCount() = 3
            override fun getColumnName(i: Int) = colNames[i]

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
                return when (columnIndex) {
                    0 -> groups[rowIndex].id
                    1 -> groups[rowIndex].name
                    2 -> groups[rowIndex].description
                    else -> null
                }
            }
        }).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(mouseEvent: MouseEvent) {
                    val table = mouseEvent.source as JTable
                    if (mouseEvent.clickCount == 2 && table.selectedRow != -1) {
                        val row = table.selectedRow
                        val id = table.getValueAt(row, 0) as Int
                        try {
                            ExistingGroupDialog(this@GroupsPanel.parent, client, id)
                        } catch (e: Exception) {
                            this@GroupsPanel.showError(java.lang.Exception("No such product. List seems to be outdated."))
                        }
                        refreshTable()
                    }
                }
            })
        }
    }
}