package com.github.ericytsang.research2016.satisfiabilitychecker.cli

import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.makeFrom

fun main(args:Array<String>)
{
    try
    {
        val proposition = Proposition.makeFrom(args[0])
        val model = proposition.models.firstOrNull()
        println(model ?: "unsatisfiable")
    }
    catch (ex:ArrayIndexOutOfBoundsException)
    {
        println("ERROR: missing command line arguments...please provide a propositional formula as command line arguments e.g. \"a and b or c iff ( not d then e nand f ) xor g\"")
    }
    catch (ex:Exception)
    {
        println("PARSING ERROR: ${ex.message}")
    }
}
