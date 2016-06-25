package com.github.ericytsang.research2016.satisfiabilitychecker.gui

import com.github.ericytsang.research2016.propositionallogic.Proposition
import com.github.ericytsang.research2016.propositionallogic.State
import com.github.ericytsang.research2016.propositionallogic.tautology
import com.github.ericytsang.research2016.propositionallogic.and
import com.github.ericytsang.research2016.propositionallogic.models
import kotlin.concurrent.thread

/**
 * Created by surpl on 5/6/2016.
 */
fun main(args:Array<String>)
{
    thread {GUI.Companion.mainLoop()}
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
        val allPropositions = gui.propositions.fold<Proposition,Proposition>(tautology)
        {
            result,nextProposition ->
            return@fold result and nextProposition
        }
        val models = allPropositions.models
        val trueSituations = models.firstOrNull()?.let {setOf(it)} ?: emptySet<State>()
        synchronized(gui)
        {
            if (!Thread.interrupted())
            {
                gui.output = trueSituations
            }
        }
    }
}
