package frontend.swing_ui

import db.entities.Group
import db.entities.query_types.ProductChange
import frontend.HttpClientLogic
import frontend.swing_ui.SimpleDocumentListener.Companion.addChangeListener
import java.awt.Dimension
import java.awt.Frame
import java.awt.GridLayout
import javax.swing.*

class ProductDialog(owner: Frame, private val client: HttpClientLogic, id: Int) : JDialog(owner, "Product", true) {
    private val product = client.loadProduct(id)
    private var groups: Map<Int, Group> = client.loadGroups().associateBy(Group::id)

    private val idInput = createIdInput()
    private val nameInput = createNameInput()
    private val numberInput = createNumberInput()
    private val priceInput = createPriceInput()
    private val groupsInput = createGroupInput()
    private val submitButton = createSubmitButton()


    private val labels = listOf("Id:", "Name:", "Number:", "Price:", "Group:").map { JLabel(it) }

    init {

        add(createPanel())

        minimumSize = Dimension(200, 200)
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
            layout = GridLayout(6, 1, bord, bord)

            add(labels[0])
            add(idInput)
            add(labels[1])
            add(nameInput)
            add(labels[2])
            add(numberInput)
            add(labels[3])
            add(priceInput)
            add(labels[4])
            add(groupsInput)
            add(submitButton)
        }
    }

    private fun changeRegistered() {
        submitButton.isEnabled = changed()
        println(submitButton.isEnabled)
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

    private fun createGroupInput() = JComboBox<Group>()
            .apply {
                addItem(null)
                for ((_, group) in groups) addItem(group)
                addActionListener {
                    changeRegistered()
                }
//                selectedItem = groups[product.groupId] //TODO something cheesy here
            }

    private fun createSubmitButton() = JButton("Submit change")
            .apply {
                isEnabled = false
                addActionListener {
                    client.modifyProduct(generateProductChange())
                }
            }

    private fun generateProductChange(): ProductChange {
        val selectedItem = groupsInput.selectedItem
        return ProductChange(product.id!!,
                nameInput.text.trim(),
                priceInput.value as Double?,
                numberInput.text.toIntOrNull(),
                (selectedItem as Group?)?.id
        )
    }

    private fun productChangeFromProduct(): ProductChange {
        val (name, price, id, number, groupId) = product
        return ProductChange(id!!, name, price, number, groupId)
    }

    private fun changed() = productChangeFromProduct() != generateProductChange()


}