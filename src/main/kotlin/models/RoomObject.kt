package models

data class RoomObject(
    val id: String,
    val name: String,
    val x: Int,
    val y: Int,
    val emoji: String? = null,
    val imagePath: String? = null,
    val hasTargetSound: Boolean = true,
    val width: Int = 100,
    val height: Int = 100
)