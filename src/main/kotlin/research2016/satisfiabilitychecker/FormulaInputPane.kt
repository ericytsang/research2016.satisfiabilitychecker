package research2016.satisfiabilitychecker

import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import lib.formulainterpreter.FormulaTreeFactory
import lib.formulainterpreter.FormulaTreeFactory.Symbol.*
import research2016.propositionallogic.And
import research2016.propositionallogic.BasicProposition
import research2016.propositionallogic.Contradiction
import research2016.propositionallogic.Iff
import research2016.propositionallogic.Nand
import research2016.propositionallogic.Not
import research2016.propositionallogic.Oif
import research2016.propositionallogic.Or
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.Xor
import java.util.*
import java.util.regex.Pattern

class FormulaInputPane:VBox()
{
    private val formulaEntries:MutableList<FormulaEntry> = LinkedList()
    private val forwardingEntryObserver = ForwardingEntryListener()

    var listener:IListener? = null

    private val _propositions = LinkedHashSet<Proposition>()
    val propositions:Set<Proposition> get() = _propositions

    init
    {
        // configure aesthetic properties
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())

        // add gui controls
        add(FormulaEntry())
    }

    private fun add(formulaEntry:FormulaEntry) = synchronized(this)
    {
        // add nodes to layout
        children.add(formulaEntry)

        // book keeping
        formulaEntries.add(formulaEntry)
        formulaEntry.listener = forwardingEntryObserver
    }

    private fun remove(formulaEntry:FormulaEntry) = synchronized(this)
    {
        // book keeping
        formulaEntry.listener = null

        // replace all existing forwarding entries
        val index = children.indexOf(formulaEntry)
        children.removeAt(index)
        children[index].requestFocus()
        formulaEntries.remove(formulaEntry)
    }

    interface IListener
    {
        fun added(proposition:Proposition)
        fun removed(proposition:Proposition)
    }

    private inner class ForwardingEntryListener:FormulaEntry.IListener
    {
        override fun onDataChanged(observee:FormulaEntry)
        {
            synchronized(this)
            {
                // remove the entry if it is empty and not the last one
                if (observee != formulaEntries.last() &&
                    observee.formula.isBlank())
                {
                    remove(observee)
                }

                // if the last forwarding entry is not empty, add a new one
                if (formulaEntries.last().formula.isNotBlank())
                {
                    add(FormulaEntry())
                }

                // go through all forwarding entries, and set the error flags
                // for duplicate entries, and parsing errors
                val usedLocalPorts = LinkedHashSet<Proposition>()
                formulaEntries.forEach()
                {
                    formulaEntry ->

                    formulaEntry.error = false

                    var proposition:Proposition? = null
                    var errorMessage:String? = null

                    try
                    {
                        proposition = formulaTreeFactory.parse(formulaEntry.formula.trim().split(Regex("[ ]+")))
                    }
                    catch (ex:Exception)
                    {
                        errorMessage = ex.message
                    }

                    if (proposition == null)
                    {
                        formulaEntry.error = true
                        formulaEntry.feedback = errorMessage ?: "unknown error..."
                    }
                    else if (proposition in usedLocalPorts)
                    {
                        formulaEntry.error = true
                        formulaEntry.feedback = "duplicate entry"
                    }
                    else
                    {
                        usedLocalPorts.add(proposition)
                        formulaEntry.feedback = "input interpreted as: $proposition"
                    }
                }

                // sync up the addressPairs map: resolve current address pairs
                val allAddressPairs = formulaEntries
                    .filter {it.error == false}
                    .mapNotNull {try {formulaTreeFactory.parse(it.formula.trim().split(Regex("[ ]+")))} catch (ex:Exception) {null}}

                // sync up the addressPairs map: remove old entries
                val toRemove = _propositions.filter {it !in allAddressPairs}
                _propositions.removeAll(toRemove)
                toRemove.forEach {listener?.removed(it)}

                // sync up the addressPairs map: add new entries
                val toAdd = allAddressPairs.filter {it !in _propositions}
                _propositions.addAll(toAdd)
                toAdd.forEach {listener?.added(it)}
            }
        }
    }

    val formulaTreeFactory = FormulaTreeFactory(

        object:FormulaTreeFactory.TokenInterpreter
        {
            override fun parse(word:String):FormulaTreeFactory.Symbol
            {
                val preprocessedWord = word.toLowerCase().trim()
                return when
                {
                    Pattern.matches("(iff){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERATOR,2,1)
                    Pattern.matches("(then){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERATOR,2,2)
                    Pattern.matches("(or){1}",preprocessedWord) ||
                        Pattern.matches("(xor){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERATOR,2,3)
                    Pattern.matches("(and){1}",preprocessedWord) ||
                        Pattern.matches("(nand){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERATOR,2,4)
                    Pattern.matches("(not){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERATOR,1,5)
                    Pattern.matches("(1){1}",preprocessedWord) ||
                        Pattern.matches("(0){1}",preprocessedWord) ||
                        Pattern.matches("[a-z]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPERAND,0,0)
                    Pattern.matches("[(]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.OPENING_PARENTHESIS,0,0)
                    Pattern.matches("[)]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(Type.CLOSING_PARENTHESIS,0,0)
                    else -> throw IllegalArgumentException("unrecognized token: $word")
                }
            }
        },

        object:FormulaTreeFactory.OperandFactory<Proposition>
        {
            override fun parse(word:String):Proposition
            {
                val preprocessedWord = word.toLowerCase().trim()
                return when
                {
                    Pattern.matches("(1){1}",preprocessedWord) -> Tautology
                    Pattern.matches("(0){1}",preprocessedWord) -> Contradiction
                    Pattern.matches("[a-z]{1}",preprocessedWord) -> BasicProposition.make(preprocessedWord)
                    else -> throw IllegalArgumentException("unrecognized token: $word")
                }
            }

            override fun parse(word:String,operands:List<Proposition>):Proposition
            {
                val preprocessedWord = word.toLowerCase().trim()
                return when
                {
                    Pattern.matches("(iff){1}",preprocessedWord) -> Iff(operands.first(),operands.last())
                    Pattern.matches("(then){1}",preprocessedWord) -> Oif(operands.first(),operands.last())
                    Pattern.matches("(or){1}",preprocessedWord) -> Or(operands.first(),operands.last())
                    Pattern.matches("(xor){1}",preprocessedWord) -> Xor(operands.first(),operands.last())
                    Pattern.matches("(and){1}",preprocessedWord) -> And(operands.first(),operands.last())
                    Pattern.matches("(nand){1}",preprocessedWord) -> Nand(operands.first(),operands.last())
                    Pattern.matches("(not){1}",preprocessedWord) -> Not(operands.single())
                    else -> throw IllegalArgumentException("unrecognized token: $word")
                }
            }
        })
}

private class FormulaEntry():VBox()
{
    companion object
    {
        /**
         * text used as prompts on [TextField]s.
         */
        private const val FORMULA_PROMPT = "Enter Formula Here e.g.: p and ( q then r iff s ) or t xor 0 nand not 1"
    }

    /**
     * used to access the string in [formulaTextField].
     */
    val formula:String get() = formulaTextField.text

    /**
     * used to access and manipulate the string in [feedbackLabel].
     */
    var feedback:String
        set(value) {feedbackLabel.text = value}
        get() = feedbackLabel.text

    /**
     * [TextField] for the user to input the formula.
     */
    private val formulaTextField = TextField()

    /**
     * [Label] to display how the program interprets the formula, or to display
     * errors.
     */
    private val feedbackLabel = Label()

    var error:Boolean = false

        /**
         * styles UI controls based on error flags.
         */
        set(value)
        {
            assert(Platform.isFxApplicationThread())

            if (value != field)
            {
                if (value)
                {
                    formulaTextField.styleClass.add(CSS.WARNING_CONTROL)
                    feedbackLabel.styleClass.add(CSS.WARNING_CONTROL)
                }
                else
                {
                    formulaTextField.styleClass.remove(CSS.WARNING_CONTROL)
                    feedbackLabel.styleClass.remove(CSS.WARNING_CONTROL)
                }
            }

            field = value
        }

    override fun requestFocus()
    {
        formulaTextField.requestFocus()
    }

    var listener:IListener? = null

    init
    {
        // reassign instance variables to run their setters
        error = true

        // set on action code
        formulaTextField.textProperty().addListener(InvalidationListener {listener?.onDataChanged(this)})

        // add prompt text to text fields
        formulaTextField.promptText = FORMULA_PROMPT

        // set fonts of displays
        formulaTextField.font = Font.font("Monospaced",16.0)
        feedbackLabel.font = Font.font("Monospaced",16.0)

        // add padding to root node
        padding = Insets(Dimens.KEYLINE_SMALL.toDouble())

        // add children
        children.addAll(formulaTextField,feedbackLabel)
    }

    interface IListener
    {
        fun onDataChanged(observee:FormulaEntry)
    }
}
