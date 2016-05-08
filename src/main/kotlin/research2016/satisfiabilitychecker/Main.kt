package research2016.satisfiabilitychecker

import research2016.propositionallogic.And
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import research2016.propositionallogic.Tautology
import research2016.propositionallogic.models
import kotlin.concurrent.thread

/**
 * Created by surpl on 5/6/2016.
 */
fun main(args:Array<String>)
{
    thread {GUI.mainLoop()}
    GUI.awaitInitialized()
    gui.listener = object:GUI.IListener
    {
        override fun added(proposition:Proposition)
        {
            findModelsAndPublishUnlessInterrupted()
        }

        override fun removed(proposition:Proposition)
        {
            findModelsAndPublishUnlessInterrupted()
        }
    }
}

private var workerThread:Thread = thread {}
fun findModelsAndPublishUnlessInterrupted() = synchronized(gui)
{
    // abort previous calculation
    workerThread.interrupt()
    gui.output = null

    // commence new calculation
    workerThread = thread(isDaemon = true)
    {
        val allPropositions = gui.propositions.fold<Proposition,Proposition>(Tautology)
        {
            result,nextProposition ->
            return@fold And(result,nextProposition)
        }
        val models = allPropositions.models
        val trueSituations = models.trueSituations.firstOrNull()?.let {setOf(it)} ?: emptySet<Situation>()
        if (!Thread.interrupted())
        {
            gui.output = trueSituations
        }
    }
}
