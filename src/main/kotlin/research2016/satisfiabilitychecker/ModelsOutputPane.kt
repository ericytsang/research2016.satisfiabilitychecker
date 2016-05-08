package research2016.satisfiabilitychecker

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import research2016.propositionallogic.Models
import research2016.propositionallogic.Situation

internal class ModelsOutputPane:HBox()
{
    private val IS_SATISFIABLE_LABEL:String = "satisfiable: "
    private val A_MODEL_LABEL:String = "a model: "

    private val NULL_MODEL_TEXT = "loading..."

    private val isSatisfiableDisplay = LabledText(IS_SATISFIABLE_LABEL)
    private val aModelDisplay = LabledText(A_MODEL_LABEL)

    var trueSituations:Set<Situation>? = null

        set(value)
        {
            if (value == null)
            {
                isSatisfiableDisplay.text = NULL_MODEL_TEXT
                aModelDisplay.text = NULL_MODEL_TEXT
            }
            else
            {
                isSatisfiableDisplay.text = value.isNotEmpty().toString()
                aModelDisplay.text = value.firstOrNull()
                    ?.let()
                    {
                        val strings = it.entries
                            .sortedBy {it.key.friendly}
                            .map()
                            {
                                if (it.value)
                                {
                                    "  ${it.key}"
                                }
                                else
                                {
                                    " !${it.key}"
                                }
                            }
                        strings.joinToString("")
                    } ?: "N/A"
            }
            field = value
        }

    init
    {
        // configure aesthetic properties
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())

        // configure & add child nodes
        children.add(isSatisfiableDisplay.node)
        isSatisfiableDisplay.node.padding = Insets(0.0,Dimens.KEYLINE_MEDIUM.toDouble(),0.0,0.0)
        children.add(aModelDisplay.node)
    }
}

private class LabledText(labelText:String)
{
    var text:String
        get() = textLabel.text
        set(value) {textLabel.text = value}
    private val labelLabel = Label(labelText)
    private val textLabel = Label()
    val node = VBox(labelLabel,textLabel)
    init
    {
        textLabel.font = Font.font("Monospaced",16.0)
        labelLabel.font = Font.font("Monospaced",16.0)
    }
}
