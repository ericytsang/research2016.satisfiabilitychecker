package com.github.ericytsang.research2016.satisfiabilitychecker.gui

import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import com.github.ericytsang.lib.formulainterpreter.FormulaTreeFactory
import com.github.ericytsang.lib.formulainterpreter.FormulaTreeFactory.Symbol.*
import com.github.ericytsang.research2016.propositionallogic.And
import com.github.ericytsang.research2016.propositionallogic.Variable
import com.github.ericytsang.research2016.propositionallogic.contradiction
import com.github.ericytsang.research2016.propositionallogic.Iff
import com.github.ericytsang.research2016.propositionallogic.Nand
import com.github.ericytsang.research2016.propositionallogic.Not
import com.github.ericytsang.research2016.propositionallogic.Oif
import com.github.ericytsang.research2016.propositionallogic.Or
import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.tautology
import com.github.ericytsang.research2016.propositionallogic.Xor
import com.github.ericytsang.research2016.propositionallogic.makeFrom
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
                        proposition = Proposition.makeFrom(formulaEntry.formula)
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
                    .mapNotNull {try { Proposition.makeFrom(it.formula) } catch (ex:Exception) {null}}

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
