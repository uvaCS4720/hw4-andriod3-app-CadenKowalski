package edu.nd.pmcburne.hello

import edu.nd.pmcburne.hello.data.MapPlacemark
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {
    @Test
    fun filtersVisiblePlacemarksByTag() {
        val places = listOf(
            MapPlacemark(1, "A", "desc", 0.0, 0.0, listOf("core", "academic")),
            MapPlacemark(2, "B", "desc", 0.0, 0.0, listOf("library"))
        )

        val filtered = places.filter { "core" in it.tags }

        assertEquals(listOf("A"), filtered.map { it.name })
    }

    @Test
    fun sortsTagsAlphabetically() {
        val sorted = listOf("library", "core", "academic").sorted()

        assertEquals(listOf("academic", "core", "library"), sorted)
    }
}
