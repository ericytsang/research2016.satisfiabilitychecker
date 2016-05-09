package research2016.satisfiabilitychecker.gui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import research2016.propositionallogic.Models
import research2016.propositionallogic.Proposition
import research2016.propositionallogic.Situation
import java.util.concurrent.CountDownLatch

private var _gui:GUI? = null
    set(value) = synchronized(GUI)
    {
        if (field != null) throw IllegalStateException("GUI should only be constructed once!")
        field = value
    }

val gui:GUI get() = _gui ?: throw IllegalStateException("must execute GUI.mainLoop before accessing the gui")

class GUI:Application()
{
    companion object
    {
        /**
         * main loop of the [GUI]. this function blocks when executed; beware!
         */
        val mainLoop =
            {
                Thread.currentThread().name = "guiLooper"
                launch(GUI::class.java)
            }

        private val releasedOnApplicationStarted = CountDownLatch(1)

        fun awaitInitialized()
        {
            releasedOnApplicationStarted.await()
        }
    }

    init
    {
        _gui = this
    }

    private val formulaInputPane:FormulaInputPane by lazy {FormulaInputPane()}

    private val modelsOutputPane:ModelsOutputPane by lazy {ModelsOutputPane()}

    /**
     * executed from [Application.launch]. sets up and displays the application
     * window.
     */
    override fun start(primaryStage:Stage)
    {
        // configure the stage (the window)
        primaryStage.title = "Satisfiability Evaluator"

        // configure the scene (inside the window)
        val borderPane = BorderPane()
        primaryStage.scene = Scene(borderPane,800.0,500.0)
        primaryStage.minWidth = 500.0
        primaryStage.minHeight = 500.0
        primaryStage.scene.stylesheets.add(CSS.FILE_PATH)

        borderPane.center = ScrollPane(formulaInputPane)
        (borderPane.center as ScrollPane).fitToWidthProperty().set(true)
        (borderPane.center as ScrollPane).fitToHeightProperty().set(true)
        borderPane.bottom = ScrollPane(modelsOutputPane)
        (borderPane.bottom as ScrollPane).fitToWidthProperty().set(true)
        (borderPane.bottom as ScrollPane).prefViewportHeight = 200.0

        // display the window
        primaryStage.show()

        // hook stuff up to each other: forwarding pane
        formulaInputPane.listener = forwardingPaneListener
        modelsOutputPane.requestFocus()

        // release count down latch...
        releasedOnApplicationStarted.countDown()
    }

    val propositions:Set<Proposition>
        get() = formulaInputPane.propositions

    var output:Set<Situation>?
        get() = modelsOutputPane.trueSituations
        set(value)
        {
            Platform.runLater {modelsOutputPane.trueSituations = value}
        }

    /**
     * elements in this set will be notified upon user interaction with [GUI].
     */
    var listener:IListener? = null

    /**
     * interface that [GUI] observers must implement to be notified by the [GUI]
     * upon user interaction.
     */
    interface IListener
    {
        /**
         * called when the user adds a new [AddressPair] to the [GUI].
         *
         * @return true if the persistence operation was successful; false
         * otherwise
         */
        fun added(proposition:Proposition)

        /**
         * called when the user removes an existing [AddressPair] from the
         * [GUI].
         *
         * @return true if the persistence operation was successful; false
         * otherwise
         */
        fun removed(proposition:Proposition)
    }

    private val forwardingPaneListener = object:FormulaInputPane.IListener
    {
        override fun added(proposition:Proposition)
        {
            listener?.added(proposition)
        }

        override fun removed(proposition:Proposition)
        {
            listener?.removed(proposition)
        }
    }
}
