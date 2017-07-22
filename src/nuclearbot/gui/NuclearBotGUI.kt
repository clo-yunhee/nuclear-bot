package nuclearbot.gui

import nuclearbot.client.ChatClient
import nuclearbot.client.ClientListener
import nuclearbot.client.Command
import nuclearbot.client.ImplChatClient
import nuclearbot.gui.components.ConfigPanel
import nuclearbot.gui.components.FooterPanel
import nuclearbot.gui.components.ModeratorPanel
import nuclearbot.gui.components.StatusPanel
import nuclearbot.gui.components.chat.ChatPanel
import nuclearbot.gui.components.commands.CommandPanel
import nuclearbot.gui.components.console.ConsolePanel
import nuclearbot.gui.components.console.DocumentOutputStream
import nuclearbot.gui.components.plugins.PluginPanel
import nuclearbot.gui.utils.DialogUtil
import nuclearbot.plugin.ImplPluginLoader
import nuclearbot.plugin.JavaPlugin
import nuclearbot.plugin.PluginLoader
import nuclearbot.util.HTML
import nuclearbot.util.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import javax.swing.TransferHandler
import javax.swing.WindowConstants
import javax.swing.text.PlainDocument

/**
 * Created by NuclearCoder on 2017-07-20.
 */

class NuclearBotGUI : JPanel(BorderLayout()), ClientListener {

    companion object {
        const val TAB_STATUS = 0
        const val TAB_PLUGIN = 1
        const val TAB_COMMANDS = 2
        const val TAB_CHAT = 3
        const val TAB_CONFIG = 4
        const val TAB_CONSOLE = 5
    }

    /* do that first in order to start logging as soon as possible */
    init {
        Logger.info("(GUI) Linking GUI console to system console...")
    }

    private val consoleDocument = PlainDocument().also(DocumentOutputStream.Companion::redirectSystemOut)

    /* client variables */
    var pluginLoader: PluginLoader = ImplPluginLoader()
    var isFrameClosing = false
    var isClientRunning = false
    var doRestartClient = false
    lateinit var client: ChatClient

    /* gui construction */
    init {
        Logger.info("(GUI) Constructing window...")
    }

    val container = JFrame("NuclearBot - Control Panel")

    val dialogs = DialogUtil(container)
    val popupMenu = JPopupMenu().apply { add(TransferHandler.getCopyAction()) }

    private val status = StatusPanel(this)
    private val plugins = PluginPanel(this)
    private val commands = CommandPanel(this)
    private val moderators = ModeratorPanel(this)
    private val chat = ChatPanel(this)
    private val config = ConfigPanel(this)
    private val console = ConsolePanel(this, consoleDocument)

    val body = JTabbedPane().apply {
        addTab("Status", status)
        addTab("Plugins", plugins)
        addTab("Commands", commands)
        addTab("Moderators", moderators)
        addTab("Chat", chat)
        addTab("Config", config)
        addTab("Console", console)
    }

    init {
        add(body, BorderLayout.CENTER)
        add(FooterPanel(), BorderLayout.SOUTH)

        container.run {
            isLocationByPlatform = true
            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    isFrameClosing = true
                    plugins.dispose()
                    container.dispose()
                    if (isClientRunning) {
                        stopClient()
                    }
                }
            })

            contentPane = this@NuclearBotGUI
            Dimension(640, 480).let {
                size = it
                preferredSize = it
            }
        }

        pluginChanged(pluginLoader.plugin)
    }

    fun open() {
        container.isVisible = true
        dialogs.setQueueDialogs(false)
    }

    fun selectTab(index: Int) {
        body.selectedIndex = index
    }

    fun startClient() {
        status.isStartEnabled = false

        Logger.info("(GUI) Starting client...")

        client = ImplChatClient(pluginLoader.plugin).apply { registerClientListener(this@NuclearBotGUI) }

        Thread {
            try {
                client.connect()
            } catch (e: Exception) {
                Logger.error("(GUI) Exception caught in client thread:")
                Logger.printStackTrace(e)

                doRestartClient = false
                onDisconnected(client)
                selectTab(TAB_CONSOLE)

                dialogs.error("Exception in client thread. Check console for details.", "Client error")
            }
        }.let {
            it.name = "client"
            it.start()
        }
    }

    fun stopClient() {
        Logger.info("(GUI) Stopping client...")

        status.isStopEnabled = false
        status.isRestartEnabled = false

        commands.unregisterCommands()

        client.stop()
    }

    fun clientStarted() {
        isClientRunning = true

        status.setStatusText("Running")
        status.isStartEnabled = false
        status.isStopEnabled = true
        status.isRestartEnabled = true

        chat.isSendEnabled = true

        doRestartClient = false

        if (isFrameClosing) {
            stopClient()
        } else {
            commands.registerCommands()
        }
    }

    fun clientStopped() {
        isClientRunning = false

        status.setStatusText("Not running")
        status.isStartEnabled = true
        status.isStopEnabled = false
        status.isRestartEnabled = false

        chat.isSendEnabled = false

        commands.unregisterCommands()

        if (doRestartClient) {
            startClient()
        }
    }

    fun pluginChanged(plugin: JavaPlugin?) {
        if (plugin != null) {
            // italic name if built-in plugin
            val pluginName = HTML.escapeText(plugin.name)
            val pluginClassName = plugin.className
            val pluginLabelText = "<html>${if (plugin.isBuiltin) "<em>$pluginName</em>" else pluginName}</html>"

            plugins.setPluginText(pluginLabelText, pluginClassName)
            status.setPluginText(pluginLabelText, pluginClassName)

            config.setPluginPanel(plugin.handle)

            if (isClientRunning) {
                // ask to restart if the client is already running
                val restart = JOptionPane.showConfirmDialog(container,
                        "The changes will be effective after a restart.\nRestart now?", "Restart?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                if (restart == JOptionPane.YES_OPTION) {
                    doRestartClient = true
                    stopClient()
                }
            }
        } else {
            selectTab(TAB_CONSOLE)
            dialogs.error(
                    "Couldn't load the plugin. Check console for details.",
                    "Error while loading plugin")
        }
    }

    override fun onConnected(client: ChatClient) {
        SwingUtilities.invokeLater { clientStarted() }
    }

    override fun onDisconnected(client: ChatClient) {
        SwingUtilities.invokeLater { clientStopped() }
    }

    override fun onMessage(client: ChatClient, username: String, message: String) {
        SwingUtilities.invokeLater { chat.addMessage(username, message) }
    }

    override fun onCommandRegistered(client: ChatClient, label: String, command: Command) {
        SwingUtilities.invokeLater { commands.addCommandList(label) }
    }

    override fun onCommandUnregistered(client: ChatClient, label: String) {
        SwingUtilities.invokeLater { commands.removeCommandList(label) }
    }

}