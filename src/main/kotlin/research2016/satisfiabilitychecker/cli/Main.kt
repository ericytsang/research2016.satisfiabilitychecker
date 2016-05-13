package research2016.satisfiabilitychecker.cli

import research2016.propositionallogic.models
import research2016.satisfiabilitychecker.core.propositionFactory

fun main(args:Array<String>)
{
    try
    {
        val tokens = args[0].split(Regex("[ ]+"))
        val proposition = propositionFactory.parse(tokens)
        val model = proposition.models.trueSituations.firstOrNull()
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
