package blue.melon.corf

class UsageRecorder(
    private val meltdownThreshold: Int,
    private val resetIntervalInTick: Int
) {
    // need to be per user, per world
    // locationBlockMap: location -> count
    private val operationCountMap = HashMap<Location3D, Int>()
    private var meltDownMap = HashSet<Location3D>()
    private var meltDownMapNextInterval = HashSet<Location3D>()
    private var nextReset = resetIntervalInTick

    fun recordUsage(location3D: Location3D) {
        val locationOperationCount =
            operationCountMap.getOrDefault(location3D, 0) + 1
        if (locationOperationCount == meltdownThreshold) {
            meltDownMapNextInterval.add(location3D)
            meltDownMap.add(location3D)
        }
        operationCountMap[location3D] = locationOperationCount
    }

    fun tick() {
        if (nextReset-- == 0) {
            nextReset = resetIntervalInTick
            meltDownMap = meltDownMapNextInterval
            meltDownMapNextInterval = HashSet()
            operationCountMap.clear()
        }
    }

    fun isMeltDown(location3D: Location3D): Boolean {
        return meltDownMap.contains(location3D)
    }

    fun getCurrentlyMeltdownSet(): Set<Location3D> {
        return meltDownMap
    }

    fun getCurrentlyMeltdownSetNextInterval(): Set<Location3D> {
        return meltDownMapNextInterval
    }
}