package blue.melon.corf

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class UsageRecorder(
    private val meltdownThreshold: Int,
    private val resetIntervalInTick: Int
) {
    // operationCountMap: location3d -> count
    private val operationCountMap = ConcurrentHashMap<Location3D, Int>()
    private var meltDownSet =
        Collections.newSetFromMap(ConcurrentHashMap<Location3D, Boolean>())
    private var meltDownSetNextInterval =
        Collections.newSetFromMap(ConcurrentHashMap<Location3D, Boolean>())
    private var nextReset = resetIntervalInTick

    fun recordUsage(location3D: Location3D) {
        val locationOperationCount =
            operationCountMap.getOrDefault(location3D, 0) + 1
        if (locationOperationCount >= meltdownThreshold) {
            meltDownSetNextInterval.add(location3D)
            meltDownSet.add(location3D)
        }
        operationCountMap[location3D] = locationOperationCount
    }

    fun tick() {
        if (nextReset-- == 0) {
            nextReset = resetIntervalInTick
            meltDownSet = meltDownSetNextInterval
            meltDownSetNextInterval = HashSet()
            operationCountMap.clear()
        }
    }

    fun isMeltDown(location3D: Location3D): Boolean {
        return meltDownSet.contains(location3D)
    }

    fun getCurrentlyMeltdownSet(): Set<Location3D> {
        return meltDownSet
    }

    fun getCurrentOperationCountMap(): Map<Location3D, Int> {
        return operationCountMap
    }

    fun getCurrentlyMeltdownSetNextInterval(): Set<Location3D> {
        return meltDownSetNextInterval
    }
}