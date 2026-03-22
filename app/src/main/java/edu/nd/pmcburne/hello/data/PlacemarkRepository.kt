package edu.nd.pmcburne.hello.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

private const val PLACEMARKS_URL = "https://www.cs.virginia.edu/~wxt4gm/placemarks.json"

class PlacemarkRepository(
    private val placemarkDao: PlacemarkDao
) {
    val tags: Flow<List<String>> = placemarkDao.observeTags()

    val placemarks: Flow<List<MapPlacemark>> = placemarkDao.observePlacemarkRows().map { rows ->
        rows.groupBy { it.id }.values.map { placemarkRows ->
            val first = placemarkRows.first()
            MapPlacemark(
                id = first.id,
                name = first.name,
                description = first.description,
                latitude = first.latitude,
                longitude = first.longitude,
                tags = placemarkRows.map { it.tag }.distinct().sorted()
            )
        }.sortedBy { it.name }
    }

    suspend fun syncPlacemarks() {
        val remotePlacemarks = fetchRemotePlacemarks()
        val placemarkEntities = remotePlacemarks.map { placemark ->
            PlacemarkEntity(
                id = placemark.id,
                name = placemark.name,
                description = placemark.description,
                latitude = placemark.latitude,
                longitude = placemark.longitude
            )
        }
        val tagEntities = remotePlacemarks.flatMap { placemark ->
            placemark.tags.distinct().map { tag ->
                PlacemarkTagEntity(
                    placemarkId = placemark.id,
                    tag = tag
                )
            }
        }

        placemarkDao.upsertPlacemarks(placemarkEntities)
        placemarkDao.insertTags(tagEntities)
    }

    private suspend fun fetchRemotePlacemarks(): List<RemotePlacemark> = withContext(Dispatchers.IO) {
        val connection = (URL(PLACEMARKS_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
        }

        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parsePlacemarks(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePlacemarks(response: String): List<RemotePlacemark> {
        val jsonArray = JSONArray(response)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val placemarkJson = jsonArray.getJSONObject(index)
                val centerJson = placemarkJson.getJSONObject("visual_center")
                val tagsJson = placemarkJson.getJSONArray("tag_list")
                add(
                    RemotePlacemark(
                        id = placemarkJson.getInt("id"),
                        name = placemarkJson.getString("name"),
                        description = placemarkJson.getString("description"),
                        latitude = centerJson.getDouble("latitude"),
                        longitude = centerJson.getDouble("longitude"),
                        tags = buildList {
                            for (tagIndex in 0 until tagsJson.length()) {
                                add(tagsJson.getString(tagIndex))
                            }
                        }
                    )
                )
            }
        }
    }
}

data class RemotePlacemark(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tags: List<String>
)
