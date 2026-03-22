package edu.nd.pmcburne.hello.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "placemarks")
data class PlacemarkEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
@Entity(
    tableName = "placemark_tags",
    primaryKeys = ["placemarkId", "tag"]
)
data class PlacemarkTagEntity(
    val placemarkId: Int,
    val tag: String
)
data class PlacemarkWithTags(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tag: String
)
data class MapPlacemark(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tags: List<String>
)
