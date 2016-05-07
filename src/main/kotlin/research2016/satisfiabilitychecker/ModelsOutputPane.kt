package research2016.satisfiabilitychecker

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import research2016.propositionallogic.Models

internal class ModelsOutputPane:HBox()
{
    private val TRUE_SITUATIONS_LABEL:String = "true situations: "
    private val FALSE_SITUATIONS_LABEL:String = "false situations: "

    private val NULL_MODEL_TEXT = "loading..."

    private val trueSituationsDisplay = LabledText(TRUE_SITUATIONS_LABEL)
    private val falseSituationsDisplay = LabledText(FALSE_SITUATIONS_LABEL)

    var models:Models? = null

        set(value)
        {
            if (value == null)
            {
                trueSituationsDisplay.text = NULL_MODEL_TEXT
                falseSituationsDisplay.text = NULL_MODEL_TEXT
            }
            else
            {
                trueSituationsDisplay.text = value.trueSituations
                    .map()
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
                    }
                    .sorted()
                    .joinToString(separator = "\n")
                falseSituationsDisplay.text = value.falseSituations
                    .map()
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
                    }
                    .sorted()
                    .joinToString(separator = "\n")
            }
            field = value
        }

    init
    {
        // configure aesthetic properties
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())

        // configure & add child nodes
        children.add(trueSituationsDisplay.node)
        trueSituationsDisplay.node.padding = Insets(0.0,Dimens.KEYLINE_MEDIUM.toDouble(),0.0,0.0)
        children.add(falseSituationsDisplay.node)
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
