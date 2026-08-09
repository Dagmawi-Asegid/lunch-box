package com.dagmawiasegid.lunchbox.util

/**
 * A short "why this spot might matter to you" line, computed from real
 * distances to known Poughkeepsie/Vassar landmarks — not written per
 * restaurant by hand, genuinely derived from each restaurant's real
 * coordinates.
 */
object LandmarkContext {

    private data class Landmark(val label: String, val lat: Double, val lon: Double, val radiusMeters: Double)

    private val landmarks = listOf(
        // Poughkeepsie Metro-North/Amtrak station, right on the Hudson waterfront
        Landmark("🚉 Near the train station & Hudson riverfront", 41.7008, -73.9340, 600.0),
        Landmark("🏙️ Downtown Main Street", 41.7005, -73.9280, 450.0),
        Landmark("🎓 Steps from Vassar's main gate", 41.6889, -73.8973, 350.0),
        Landmark("🎓 On/near Vassar campus", 41.6906, -73.8990, 700.0),
        Landmark("📍 Arlington, near Vassar", 41.6850, -73.9050, 900.0)
    )

    fun forLocation(latitude: Double, longitude: Double): String? {
        return landmarks
            .map { it to DistanceUtil.metersBetween(latitude, longitude, it.lat, it.lon) }
            .filter { (landmark, distance) -> distance <= landmark.radiusMeters }
            .minByOrNull { (_, distance) -> distance }
            ?.first?.label
    }
}
