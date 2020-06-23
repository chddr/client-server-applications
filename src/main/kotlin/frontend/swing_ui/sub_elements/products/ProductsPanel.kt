package frontend.swing_ui.sub_elements.products

import db.entities.Criterion
import db.entities.Group
import db.entities.Product
import frontend.http.HttpClientLogic
import frontend.swing_ui.sub_elements.products.ProductsPanel.UpDown.Down
import frontend.swing_ui.sub_elements.products.ProductsPanel.UpDown.Up
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel


class ProductsPanel(private val client: HttpClientLogic, private val parent: JFrame) : JPanel() {
    companion object {
        private const val SIZE: Int = 10
        private val colNames = arrayOf("Id", "Price", "Name", "Number", "Group")
    }

    /*Info*/
    private var prods = ArrayList<Product>(25)
    private var groups: Map<Int, Group> = HashMap()
    private val criterion = Criterion()
    private var page = 0

    enum class UpDown { Up, Down }

    /*Swing elements*/
    private val addProduct = createAddProductButton()
    private val queryInput = JTextField(10)
    private val lowerBound = JTextField(10)
    private val upperBound = JTextField(10)
    private val groupsInput = JComboBox<Group>().apply { addItem(null) }

    private var pageNum: JLabel = JLabel(page.toString()).apply {
        font = font.deriveFont(18f)
    }
    private var table = createTable()

    init {
        layout = BorderLayout(5, 5)
        add(createQueryPanel(), BorderLayout.WEST)
        add(createPagePanel(), BorderLayout.SOUTH)
        add(JScrollPane(table), BorderLayout.CENTER)
    }

    private fun updatePageCount(upDown: UpDown) {
        when (upDown) {
            Up -> page++
            Down -> page = (--page).coerceAtLeast(0)
        }
        pageNum.text = page.toString()
        refreshTable()
    }

    private fun refreshTable() {
        loadGroups()
        pageNum.text = page.toString()
        try {
            prods = client.loadProducts(page, SIZE, criterion)
            table.updateUI()
        } catch (e: Exception) {
            showError(e)
        }
    }

    private fun loadGroups() {
        try {
            groups = client.loadGroups().associateBy(Group::id)
            val prev = groupsInput.selectedItem as Group?
            groupsInput.removeAllItems()
            groupsInput.addItem(null)
            for ((_, group) in groups) {
                groupsInput.addItem(group)
            }
            groupsInput.selectedItem = prev
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

    private fun createPagePanel(): JPanel {
        return JPanel().apply {
            layout = FlowLayout(FlowLayout.CENTER)

            add(JButton("<").apply {
                addActionListener { updatePageCount(Down) }
            })

            add(pageNum)

            add(JButton(">").apply {
                addActionListener { updatePageCount(Up) }
            })
        }
    }

    private fun JComponent.al(): JComponent {
        alignmentX = Component.LEFT_ALIGNMENT
        return this
    }

    private fun createQueryPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(addProduct.al())
            add(JLabel("Query:").al())
            add(queryInput.al())
            add(JLabel("Lower bound:").al())
            add(lowerBound.al())
            add(JLabel("Upper bound:").al())
            add(upperBound.al())
            add(JLabel("Group:").al())
            add(groupsInput.al())
            add(Box.createVerticalStrut(5))
            add(Box.createVerticalGlue())

            add(JButton("Refresh").apply {
                al()
                addActionListener {
                    loadProductsAndGroups()
                }
            })
        }
    }

    private fun loadProductsAndGroups() {
        val (lower, upper) = try {
            val lower = if (lowerBound.text.isBlank()) {
                null
            } else lowerBound.text.toDouble()
            val upper = if (upperBound.text.isBlank()) {
                null
            } else upperBound.text.toDouble()
            lower to upper
        } catch (e: Exception) {
            this@ProductsPanel.showError(java.lang.Exception("Please input correct numeric bounds"))
            return
        }

        criterion.query(queryInput.text.trim())
                .lower(lower)
                .upper(upper)
                .groupId((groupsInput.selectedItem as Group?)?.id)
        refreshTable()
    }

    private fun createAddProductButton(): JButton {
        return JButton("Create new product").apply {
            background = Color.GREEN.darker()
            foreground = Color.WHITE
            addActionListener {
                addGroup()
            }
        }
    }

    private fun addGroup() {
        try {
            NewProductDialog(this@ProductsPanel.parent, client)
            refreshTable()
        } catch (e: Exception) {
            this@ProductsPanel.showError(e)
        }
    }

    private fun createTable(): JTable {
        val table = JTable(object : AbstractTableModel() {
            override fun getRowCount() = prods.size
            override fun getColumnCount() = 5
            override fun getColumnName(i: Int) = colNames[i]

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
                return when (columnIndex) {
                    0 -> prods[rowIndex].id
                    1 -> prods[rowIndex].price
                    2 -> prods[rowIndex].name
                    3 -> prods[rowIndex].number
                    4 -> groups[prods[rowIndex].groupId]
                    else -> null
                }
            }
        })
        table.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val t = mouseEvent.source as JTable
                if (mouseEvent.clickCount == 2 && t.selectedRow != -1) {
                    val row = t.selectedRow
                    val id = t.getValueAt(row, 0) as Int
                    try {
                        ExistingProductDialog(this@ProductsPanel.parent, client, id)
                    } catch (e: Exception) {
                        this@ProductsPanel.showError(java.lang.Exception("No such product. List seems to be outdated."))
                    }
                    refreshTable()
                }
            }
        })
        return table
    }

}
