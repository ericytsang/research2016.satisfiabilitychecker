package research2016.satisfiabilitychecker.core

import lib.formulainterpreter.FormulaTreeFactory
import research2016.propositionallogic.*
import java.util.regex.Pattern

/**
 * Created by Eric on 5/9/2016.
 */

fun prepareForPropositionFactory(string:String):List<String> = string.replace("("," ( ").replace(")"," ) ").replace("-"," - ").trim().split(Regex("[ ]+"))

val propositionFactory = FormulaTreeFactory(

    object:FormulaTreeFactory.TokenInterpreter
    {
        override fun parse(word:String):FormulaTreeFactory.Symbol
        {
            val preprocessedWord = word.toLowerCase().trim()
            return when
            {
                Pattern.matches("(iff){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,1)
                Pattern.matches("(then){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,2)
                Pattern.matches("(or){1}",preprocessedWord) ||
                    Pattern.matches("(xor){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,3)
                Pattern.matches("(and){1}",preprocessedWord) ||
                    Pattern.matches("(nand){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,2,4)
                Pattern.matches("(-){1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERATOR,1,5)
                Pattern.matches("(1){1}",preprocessedWord) ||
                    Pattern.matches("(0){1}",preprocessedWord) ||
                    Pattern.matches("[a-zA-Z]+",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPERAND,0,0)
                Pattern.matches("[(]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.OPENING_PARENTHESIS,0,0)
                Pattern.matches("[)]{1}",preprocessedWord) -> FormulaTreeFactory.Symbol(FormulaTreeFactory.Symbol.Type.CLOSING_PARENTHESIS,0,0)
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
                Pattern.matches("[a-zA-Z]+",preprocessedWord) -> BasicProposition.make(preprocessedWord)
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
                Pattern.matches("(-){1}",preprocessedWord) -> Not(operands.single())
                else -> throw IllegalArgumentException("unrecognized token: $word")
            }
        }
    })