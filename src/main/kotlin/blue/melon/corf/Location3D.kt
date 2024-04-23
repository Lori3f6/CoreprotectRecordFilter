package blue.melon.corf

import org.bukkit.Location

data class Location3D(val x: Int, val y: Int, val z: Int) {

    fun compress(): Long {
        return getLocationCompressed(x, y, z);
    }

    companion object {
        fun getLocationDecompressed(location: Long): Location3D {
            val x = ((location shr 38) and 0x3FFFFFFL).toInt() - 30000000
            val y = ((location shr 12) and 0x3FFFFFFL).toInt() - 30000000
            val z = ((location) and 0xFFF).toInt() - 64
            return Location3D(x, y, z)
        }

        // location compressed:
        // x and y has int range from -30000000 to 30000000, make it unsigned by add 30000000
        // z has int range from -64 to 2032, make it unsigned by add 64
        // x, y will no more than 26 bits, z no more than 12 bits,
        // so we can compress it into 64 bits
        fun getLocationCompressed(x: Int, y: Int, z: Int): Long {
            return (((x + 30000000).toLong()) and 0x3FFFFFFL shl 38) or
                    (((y + 30000000).toLong()) and 0x3FFFFFFL shl 12) or
                    (z + 64).toLong()
        }

        fun getLocationCompressed(location: Location): Long {
            return getLocationCompressed(
                location.blockX,
                location.blockY,
                location.blockZ
            )
        }
    }


}
