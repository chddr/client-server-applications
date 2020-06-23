package frontend.swing_ui

import db.entities.Group
import db.entities.Product
import frontend.HttpClientLogic
import frontend.swing_ui.UiUtils.showError
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

@Suppress("DuplicatedCode") // a lot of UI code
class NewProductDialog(parent: JFrame, private val client: HttpClientLogic) : JDialog(parent, "Create a new product", true) {

    private var groups: Map<Int, Group> = client.loadGroups().associateBy(Group::id)

    private val nameInput = createNameInput()
    private val numberInput = createNumberInput()
    private val priceInput = createPriceInput()
    private val groupsInput = createGroupInput()
    private val createButton = createCreateButton()

    private val labels = listOf("Name:", "Price:", "Number:", "Group:").map { JLabel(it) }

    init {

        add(createPanel())

        minimumSize = Dimension(200, 200)
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
            layout = GridLayout(5, 1, bord, bord)

            add(labels[0])
            add(nameInput)
            add(labels[1])
            add(numberInput)
            add(labels[2])
            add(priceInput)
            add(labels[3])
            add(groupsInput)
            add(createButton)
        }
    }

    private fun createNameInput() = JTextField()

    private fun createNumberInput() = JTextField()

    private fun createPriceInput() = JSpinner(SpinnerNumberModel(1.0, 0.0, 10_000.0, 0.1))

    private fun createGroupInput() = JComboBox<Group>()
            .apply {
                addItem(null)
                for ((_, group) in groups) addItem(group)
            }

    private fun createCreateButton() = JButton("Submit change")
            .apply {
                addActionListener {
                    try {
                        client.createProduct(formProduct())
                        dispose()
                    } catch (e: Exception) {
                        this@NewProductDialog.showError(e)
                    }

                }
            }

    private fun formProduct() = Product(
            name = nameInput.text.trim(),
            price = priceInput.value as Double,
            number = numberInput.text.toIntOrNull(),
            groupId = (groupsInput.selectedItem as Group?)?.id
    )


}
