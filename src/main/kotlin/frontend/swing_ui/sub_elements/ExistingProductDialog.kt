package frontend.swing_ui.sub_elements

import db.entities.Group
import db.entities.query_types.ProductChange
import frontend.HttpClientLogic
import frontend.swing_ui.utils.SimpleDocumentListener.Companion.addChangeListener
import frontend.swing_ui.utils.UiUtils.showError
import java.awt.Color
import java.awt.Dimension
import java.awt.Frame
import java.awt.GridLayout
import javax.swing.*

@Suppress("DuplicatedCode")
class ExistingProductDialog(owner: Frame, private val client: HttpClientLogic, id: Int) : JDialog(owner, "Product", true) {
    private val product = client.loadProduct(id)
    private var groups: Map<Int, Group> = client.loadGroups().associateBy(Group::id)

    private val idInput = createIdInput()
    private val nameInput = createNameInput()
    private val numberInput = createNumberInput()
    private val priceInput = createPriceInput()
    private val submitButton = createSubmitButton()
    private val deleteButton = createDeleteButton()
    private val groupsInput = createGroupInput()


    private val labels = listOf("Id:", "Name:", "Price:", "Number:", "Group:").map { JLabel(it) }

    init {

        add(createPanel())

        minimumSize = Dimension(200, 200)
        isResizable = false
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        changeRegistered()
        pack()
        isVisible = true
    }

    private fun createPanel(): JPanel {
        val bord = 5 // border size
        return JPanel().apply {
            border = BorderFactory.createEmptyBorder(bord, bord, bord, bord)
            layout = GridLayout(6, 2, bord, bord)

            add(labels[0])
            add(idInput)
            add(labels[1])
            add(nameInput)
            add(labels[2])
            add(priceInput)
            add(labels[3])
            add(numberInput)
            add(labels[4])
            add(groupsInput)
            add(submitButton)
            add(deleteButton)
        }
    }

    private fun changeRegistered() {
        submitButton.isEnabled = changed()
    }

    /*
    Very big
    ugly
    declarative
    methods
     */

    private fun createIdInput() = JTextField(product.id.toString())
            .apply {
                isEditable = false
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createNameInput() = JTextField(product.name)
            .apply {
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createNumberInput() = JTextField(product.number.toString())
            .apply {
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createPriceInput() = JSpinner(SpinnerNumberModel(product.price, 0.0, 10_000.0, 0.1))
            .apply {
                addChangeListener {
                    changeRegistered()
                }
            }

    private fun createGroupInput(): JComboBox<Group> {
        val jComboBox = JComboBox<Group>()
        return jComboBox.apply {
            addItem(null)
            for ((_, group) in groups) addItem(group)

            if (product.groupId != null) {
                val group = groups[product.groupId]
                selectedItem = group
            }


            addActionListener {
                changeRegistered()
            }


        }
    }

    private fun createSubmitButton() = JButton("Submit change")
            .apply {
                isEnabled = false
                addActionListener {
                    try {
                        client.modifyProduct(generateProductChange())
                        dispose()
                    } catch (e: Exception) {
                        this@ExistingProductDialog.showError(e)
                    }

                }
            }

    private fun createDeleteButton() = JButton("Delete product")
            .apply {
                background = Color.RED.darker()
                foreground = Color.BLACK.brighter()
                addActionListener {
                    val response = JOptionPane.showConfirmDialog(this@ExistingProductDialog, "Do you really want to delete the product?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                    if (response == JOptionPane.NO_OPTION) return@addActionListener
                    try {
                        client.deleteProduct(product.id!!)
                        dispose()
                    } catch (e: Exception) {
                        this@ExistingProductDialog.showError(e)
                    }

                }
            }

    private fun generateProductChange(): ProductChange {
        var name: String? = nameInput.text.trim()
        name = if (product.name == name) null else name

        var price = priceInput.value as Double?
        price = if (product.price == price) null else price

        var number = numberInput.text.toIntOrNull()
        number = if (product.number == number) null else number

        var groupId = (groupsInput.selectedItem as Group?)?.id
        groupId = if (product.groupId == groupId) null else groupId

        return ProductChange(product.id!!,
                name,
                price,
                number,
                groupId
        )
    }

    private fun changed(): Boolean {
        generateProductChange().run {
            return (name != null || price != null || number != null || groupId != null || (product.groupId != (groupsInput.selectedItem as Group?)?.id))
        }
    }


}