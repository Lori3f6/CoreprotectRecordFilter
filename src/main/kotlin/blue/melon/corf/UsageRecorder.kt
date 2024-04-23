package blue.melon.corf

import java.util.*

class UsageRecorder(
    private val meltdownThreshold: Int,
    private val resetIntervalInTick: Int
) {
    // need to be per user, per world
    // locationBlockMap: worldUUID - > map(location -> count)
    private val operationCountMap = HashMap<UUID, HashMap<Long, Int>>()
    private var meltDownMap = HashMap<UUID, HashSet<Long>>()
    private var meltDownMapNextInterval = HashMap<UUID, HashSet<Long>>()
    private var nextReset = resetIntervalInTick;

    fun recordUsage(worldUniqueID: UUID, locCompressed: Long) {
        val locationOperationCount =
            operationCountMap.getOrPut(worldUniqueID) { HashMap() }
        val count = locationOperationCount.getOrDefault(
            locCompressed, 0
        ) + 1
        if (count == meltdownThreshold) {

            val meltDownNextIntervalSet = meltDownMapNextInterval.getOrPut(worldUniqueID) { HashSet() }
            meltDownNextIntervalSet.add(locCompressed)

            val meltDownCurrentIntervalSet = meltDownMap.getOrPut(worldUniqueID) { HashSet() }
            meltDownCurrentIntervalSet.add(locCompressed)
        }
        locationOperationCount[locCompressed] = count
    }

    fun tick() {
        if (nextReset-- == 0) {
            nextReset = resetIntervalInTick
            meltDownMap = meltDownMapNextInterval
            meltDownMapNextInterval = HashMap()
            operationCountMap.clear()
        }
    }

    fun isMeltDown(worldUniqueID: UUID, locCompressed: Long): Boolean {
        if (!meltDownMap.containsKey(worldUniqueID)) return false
        return meltDownMap[worldUniqueID]!!.contains(locCompressed)
    }

    fun getCurrentlyMeltdownMap(): Map<UUID, Set<Long>> {
        return meltDownMap
    }

    fun getCurrentlyMeltdownMapNextInterval(): Map<UUID, Set<Long>> {
        return meltDownMapNextInterval
    }
}