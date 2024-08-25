package blue.melon.corf

import blue.melon.corf.config.Config
import com.google.gson.GsonBuilder
import net.coreprotect.event.CoreProtectBlockBreakPreLogEvent
import net.coreprotect.event.CoreProtectBlockPlacePreLogEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class SpigotLoader : Listener, JavaPlugin(), CommandExecutor {
    // worldUserUsageMap: user -> UsageRecorder
    private val usageRecorders = ConcurrentHashMap<String, UsageRecorder>()
    private lateinit var config: Config
    private var blockedRecords = 0L
    private var recordProcessed = 0L
    private var lastReload = 0L

    override fun onEnable() {
        dataFolder.mkdir()
        reload(Bukkit.getConsoleSender())

        server.pluginManager.registerEvents(this, this)
        server.getPluginCommand("corf")?.setExecutor(this)

        server.globalRegionScheduler.runAtFixedRate(this, { _ ->
            usageRecorders.values.forEach(UsageRecorder::tick)
        }, 1, 1)
    }

    private fun reload(sender: CommandSender) {
        val configFile = File(dataFolder, "config.json")
        val gson =
            GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        if (!configFile.exists()) {
            configFile.createNewFile()
        }
        blockedRecords = 0L
        recordProcessed = 0L
        //load config
        config =
            gson.fromJson(configFile.readText(), Config::class.java) ?: Config()
        //save updated config
        configFile.writeText(gson.toJson(config))
        usageRecorders.clear()
        lastReload = System.currentTimeMillis()
        //initialize user map
        config.usageLimit.forEach { (userName, limit) ->
            usageRecorders[userName] = UsageRecorder(
                limit.meltdownThreshold,
                limit.resetIntervalInTick
            )
            val limitPlacedMessage = Component.text(
                "Placed filter threshold for $userName -> ${limit.meltdownThreshold} per ${limit.resetIntervalInTick} ticks",
                NamedTextColor.DARK_GRAY
            )
            sender.sendMessage(limitPlacedMessage)
        }
    }


    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        if (!sender.hasPermission("corf.admin")) {
            sender.sendMessage(
                Component.text("permission denied to use this command")
                    .color(NamedTextColor.RED)
            )
            return true
        }
        if (args == null || args.isEmpty() || args[0] != "reload") {
            // filter usage
            // show hotspots and blocked records
            sender.sendMessage(
                Component.text(
                    "CORF Activation:",
                    NamedTextColor.DARK_GRAY
                )
            )
            var hotSpots = 0
            var totalMapSize = 0
            usageRecorders.forEach { (user, recorder) ->
                totalMapSize += recorder.getCurrentOperationCountMap().size
                recorder.getCurrentlyMeltdownSet().forEach() { location3D ->
                    hotSpots++
                    val world = Bukkit.getWorld(location3D.worldUniqueID)
                    sender.sendMessage(
                        Component.text("  - ", NamedTextColor.DARK_GRAY).append(
                            Component.text(user, NamedTextColor.DARK_AQUA)
                        )
                            .append(
                                if (world == null) {
                                    Component.text(
                                        "@invalid",
                                        NamedTextColor.RED
                                    )
                                } else {
                                    Component.text(
                                        "@${
                                            Bukkit.getWorld(
                                                location3D.worldUniqueID
                                            )?.name
                                        }", NamedTextColor.DARK_GRAY
                                    )
                                }
                            ).append(
                                Component.text(
                                    " @",
                                    NamedTextColor.DARK_AQUA
                                )
                            ).append(
                                Component.text(
                                    " x:",
                                    NamedTextColor.DARK_GRAY
                                )
                            )
                            .append(
                                Component.text(
                                    location3D.x,
                                    NamedTextColor.DARK_AQUA
                                )
                            )
                            .append(
                                Component.text(
                                    " y:",
                                    NamedTextColor.DARK_GRAY
                                )
                            )
                            .append(
                                Component.text(
                                    location3D.y,
                                    NamedTextColor.DARK_AQUA
                                )
                            )
                            .append(
                                Component.text(
                                    " z:",
                                    NamedTextColor.DARK_GRAY
                                )
                            )
                            .append(
                                Component.text(
                                    location3D.z,
                                    NamedTextColor.DARK_AQUA
                                )
                            ).clickEvent(
                                ClickEvent.runCommand("/co teleport ${world?.name ?: "invalid"} ${location3D.x} ${location3D.y} ${location3D.z}")
                            ).hoverEvent(
                                Component.text(
                                    "#${hotSpots}",
                                    NamedTextColor.DARK_AQUA
                                ).append(
                                    Component.text(
                                        " (Click to Teleport)",
                                        NamedTextColor.DARK_GRAY
                                    )
                                )
                            )
                    )

                }
            }
            sender.sendMessage(
                Component.text(hotSpots, NamedTextColor.DARK_AQUA).append(
                    Component.text(
                        " hotspots detected",
                        NamedTextColor.DARK_GRAY
                    )
                )
            )
            sender.sendMessage(
                Component.text(
                    "Total record map size: ",
                    NamedTextColor.DARK_GRAY
                )
                    .append(
                        Component.text(totalMapSize, NamedTextColor.DARK_AQUA)
                    )
            )
            sender.sendMessage(
                Component.text("Processed records: ", NamedTextColor.DARK_GRAY)
                    .append(
                        Component.text(
                            recordProcessed,
                            NamedTextColor.DARK_AQUA
                        )
                    )
            )

            sender.sendMessage(
                Component.text("Filtered records: ", NamedTextColor.DARK_GRAY)
                    .append(
                        Component.text(blockedRecords, NamedTextColor.DARK_AQUA)
                    ).append(
                        Component.text(
                            " (${
                                if (recordProcessed == 0L) "0" else String.format(
                                    "%.2f",
                                    blockedRecords.toDouble() / recordProcessed * 100
                                )
                            }%)", NamedTextColor.DARK_GRAY
                        )
                    )
            )
            return true
        }
        // else /corf reload
        reload(sender)
        sender.sendMessage(
            Component.text(
                "corf reloaded",
                NamedTextColor.DARK_GRAY
            )
        )
        return true
    }


    @EventHandler(ignoreCancelled = true)
    fun onCoreProtectPreLogBreak(event: CoreProtectBlockBreakPreLogEvent) {
        val usageRecorder = usageRecorders[event.user] ?: return
        val location3d = Location3D.fromBukkitLocation(event.location)
        usageRecorder.recordUsage(location3d)
        recordProcessed++
        if (usageRecorder.isMeltDown(location3d)) {
            event.isCancelled = true
            blockedRecords++
        }
    }

    @EventHandler
    fun onCoreProtectPreLogPlace(event: CoreProtectBlockPlacePreLogEvent) {
        val usageRecorder = usageRecorders[event.user] ?: return
        val location3d = Location3D.fromBukkitLocation(event.location)
        usageRecorder.recordUsage(location3d)
        recordProcessed++
        if (usageRecorder.isMeltDown(location3d)) {
            event.isCancelled = true
            blockedRecords++
        }
    }

}