package edu.nd.pmcburne.hello.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacemarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlacemarks(placemarks: List<PlacemarkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<PlacemarkTagEntity>)

    @Query(
        """
        SELECT p.id, p.name, p.description, p.latitude, p.longitude, t.tag
        FROM placemarks p
        INNER JOIN placemark_tags t ON p.id = t.placemarkId
        ORDER BY p.name ASC
        """
    )
    fun observePlacemarkRows(): Flow<List<PlacemarkWithTags>>

    @Query("SELECT DISTINCT tag FROM placemark_tags ORDER BY tag ASC")
    fun observeTags(): Flow<List<String>>
}
