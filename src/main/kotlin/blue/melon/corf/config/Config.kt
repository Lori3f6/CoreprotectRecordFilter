package blue.melon.corf.config

data class Config(val usageLimit:Map<String, UserSetting> = mutableMapOf(
    "#lava" to UserSetting(10, 20 * 60),
    "#wither" to UserSetting(10, 20 * 60),
    "#dispenser" to UserSetting(10, 20 * 60),
    "#water" to UserSetting(10, 20 * 60),
    "#bamboo" to UserSetting(10, 20 * 60),
    "#snowman" to UserSetting(10, 20 * 60)
))
