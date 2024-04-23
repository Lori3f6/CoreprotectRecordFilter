package blue.melon.corf

import blue.melon.corf.config.Config
import com.google.gson.GsonBuilder
import net.coreprotect.event.CoreProtectBlockBreakPreLogEvent
import net.coreprotect.event.CoreProtectBlockPlacePreLogEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SpigotLoader : Listener, JavaPlugin() {
    // worldUserUsageMap: user -> UsageRecorder
    private val usageRecorders = HashMap<String, UsageRecorder>()
    private lateinit var config: Config
    private var blockedRecords = 0

    override fun onEnable() {
        dataFolder.mkdir()
        val configFile = File(dataFolder, "config.json")
        val gson =
            GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        if (!configFile.exists()) {
            configFile.createNewFile()
        }
        //load config
        config =
            gson.fromJson(configFile.readText(), Config::class.java) ?: Config()
        //save config
        configFile.writeText(gson.toJson(config))

        //initialize user map
        config.usageLimit.forEach { (userName, limit) ->
            usageRecorders[userName] = UsageRecorder(
                limit.meltdownThreshold,
                limit.resetIntervalInTick
            )
        }

        server.pluginManager.registerEvents(this, this)
        server.globalRegionScheduler.runAtFixedRate(this, { _ ->
            usageRecorders.values.forEach(UsageRecorder::tick)
        }, 1, 1)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCoreProtectPreLogBreak(event: CoreProtectBlockBreakPreLogEvent) {
        if (!usageRecorders.containsKey(event.user)) return
        val usageRecorder = usageRecorders[event.user]!!
        val worldUniqueId = event.location.world.uid
        val locationCompressed = Location3D.getLocationCompressed(event.location)
        usageRecorder.recordUsage(worldUniqueId, locationCompressed)
        if (usageRecorder.isMeltDown(worldUniqueId,locationCompressed)) {
            event.isCancelled = true
            logger.info("Blocked record: ${event.user} ${Location3D.getLocationDecompressed(locationCompressed)})")
            blockedRecords++
        }
    }

    @EventHandler
    fun onCoreProtectPreLogPlace(event: CoreProtectBlockPlacePreLogEvent) {
        if (!usageRecorders.containsKey(event.user)) return
        val usageRecorder = usageRecorders[event.user]!!
        val worldUniqueId = event.location.world.uid
        val locationCompressed = Location3D.getLocationCompressed(event.location)
        usageRecorder.recordUsage(worldUniqueId, locationCompressed)
        if (usageRecorder.isMeltDown(worldUniqueId,locationCompressed)) {
            event.isCancelled = true
            logger.info("Blocked record: ${event.user} ${Location3D.getLocationDecompressed(locationCompressed)})")
            blockedRecords++
        }
    }

}