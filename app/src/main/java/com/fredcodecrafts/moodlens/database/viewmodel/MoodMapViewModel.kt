package com.fredcodecrafts.moodlens.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredcodecrafts.moodlens.database.repository.MoodMapRepository
import com.fredcodecrafts.moodlens.ui.screens.MoodCluster
import com.fredcodecrafts.moodlens.ui.screens.MoodLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel for the Mood Map feature.
 * Handles mood location data and clustering logic.
 */
class MoodMapViewModel(
    private val repository: MoodMapRepository,
    private val userId: String
) : ViewModel() {

    private val _moodLocations = MutableStateFlow<List<MoodLocation>>(emptyList())
    val moodLocations: StateFlow<List<MoodLocation>> = _moodLocations.asStateFlow()

    private val _clusters = MutableStateFlow<List<MoodCluster>>(emptyList())
    val clusters: StateFlow<List<MoodCluster>> = _clusters.asStateFlow()

    private val CLUSTERING_DISTANCE_KM = 0.5 // 500 meters radius for clustering

    init {
        loadMoodLocations()
    }

    /**
     * Loads all mood locations for the current user from the repository.
     */
    private fun loadMoodLocations() {
        viewModelScope.launch {
            repository.getMoodLocationsForUser(userId).collect { locations ->
                _moodLocations.value = locations
                _clusters.value = clusterMoodLocations(locations)
            }
        }
    }

    /**
     * Clusters mood locations based on proximity.
     * If 5 or more entries are within CLUSTERING_DISTANCE_KM, they form a cluster.
     *
     * @param locations List of all mood locations to cluster
     * @return List of clusters with their dominant moods
     */
    private fun clusterMoodLocations(locations: List<MoodLocation>): List<MoodCluster> {
        if (locations.isEmpty()) return emptyList()

        val clusters = mutableListOf<MoodCluster>()
        val processed = mutableSetOf<Long>()

        for (location in locations) {
            if (location.id in processed) continue

            // Find all nearby locations within clustering distance
            val nearbyLocations = locations.filter { other ->
                other.id !in processed &&
                        calculateDistance(
                            location.latitude, location.longitude,
                            other.latitude, other.longitude
                        ) <= CLUSTERING_DISTANCE_KM
            }

            // If 5 or more locations are nearby, create a cluster
            if (nearbyLocations.size >= 5) {
                val centerLat = nearbyLocations.map { it.latitude }.average()
                val centerLng = nearbyLocations.map { it.longitude }.average()
                val dominantMood = findDominantMood(nearbyLocations)

                clusters.add(
                    MoodCluster(
                        latitude = centerLat,
                        longitude = centerLng,
                        moods = nearbyLocations,
                        dominantMood = dominantMood
                    )
                )

                processed.addAll(nearbyLocations.map { it.id })
            } else {
                // Create individual cluster for this location
                clusters.add(
                    MoodCluster(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        moods = listOf(location),
                        dominantMood = location.mood
                    )
                )
                processed.add(location.id)
            }
        }

        return clusters
    }

    /**
     * Finds the most common mood in a list of mood locations.
     *
     * @param locations List of mood locations to analyze
     * @return The most frequently occurring mood
     */
    private fun findDominantMood(locations: List<MoodLocation>): String {
        return locations
            .groupBy { it.mood }
            .maxByOrNull { it.value.size }
            ?.key ?: "Neutral"
    }

    /**
     * Calculates the distance between two geographical points using Haversine formula.
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    /**
     * Filters mood locations by a specific mood type.
     * Pass null to show all moods.
     *
     * @param mood The mood to filter by, or null for all moods
     */
    fun filterByMood(mood: String?) {
        viewModelScope.launch {
            val allLocations = _moodLocations.value
            val filtered = if (mood != null) {
                allLocations.filter { it.mood.equals(mood, ignoreCase = true) }
            } else {
                allLocations
            }
            _clusters.value = clusterMoodLocations(filtered)
        }
    }
}
/**
 * Factory for creating MoodMapViewModel instances.
 * Required because the ViewModel has constructor parameters.
 */
class MoodMapViewModelFactory(
    private val repository: MoodMapRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodMapViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}