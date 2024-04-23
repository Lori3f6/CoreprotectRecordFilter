package blue.melon.corf

import org.bukkit.Location
import java.util.*

data class Location3D(val worldUniqueID: UUID, val x: Int, val y: Int, val z: Int) {
    companion object {
        fun fromBukkitLocation(location: Location): Location3D {
            return Location3D(
                location.world.uid,
                location.blockX,
                location.blockY,
                location.blockZ
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location3D

        if (worldUniqueID != other.worldUniqueID) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = worldUniqueID.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }
}
