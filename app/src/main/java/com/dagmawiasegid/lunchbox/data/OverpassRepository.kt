package com.dagmawiasegid.lunchbox.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

data class NearbyPlace(
    val osmId: String,
    val name: String,
    val cuisine: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?
)

/**
 * Finds nearby restaurants via OpenStreetMap's free Overpass API — no
 * billing, no API key, no rate-limit signup. Trade-off vs. Google Places:
 * no ratings/review counts and photos are rare (only when a contributor
 * added an `image` tag), coverage varies more by area, but it's genuinely
 * free with no usage cap tied to a credit card.
 */
class OverpassRepository {

    private val client = OkHttpClient()

    suspend fun searchNearby(latitude: Double, longitude: Double, radiusMeters: Int = 1500): List<NearbyPlace> =
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            // The public Overpass instance is shared/free and occasionally
            // rejects requests under load ("server too busy") — a short
            // retry smooths over that without depending on a second mirror
            // server (tested one; it was slower/less reliable than just
            // retrying the primary).
            repeat(2) { attempt ->
                try {
                    return@withContext fetchOnce(latitude, longitude, radiusMeters)
                } catch (e: Exception) {
                    lastError = e
                    if (attempt == 0) delay(3000)
                }
            }
            throw lastError ?: Exception("Overpass request failed")
        }

    private fun fetchOnce(latitude: Double, longitude: Double, radiusMeters: Int): List<NearbyPlace> {
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);
            );
            out center tags;
        """.trimIndent()

        val body = FormBody.Builder().add("data", query).build()
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            // Overpass's usage policy asks clients to identify themselves;
            // some deployments also reject generic HTTP-client user agents
            // outright (this app was getting HTTP 406 without this).
            .addHeader("User-Agent", "LunchBoxApp/1.0 (Android; github.com/Dagmawi-Asegid/lunch-box)")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw Exception("Overpass API responded ${response.code}")
            }
            if (!text.trimStart().startsWith("{")) {
                throw Exception("Overpass server is busy — try again shortly")
            }
            val root = JSONObject(text)
            val elements = root.optJSONArray("elements") ?: return emptyList()

            return (0 until elements.length()).mapNotNull { i ->
                parseElement(elements.getJSONObject(i))
            }
        }
    }

    private fun parseElement(element: JSONObject): NearbyPlace? {
        val tags = element.optJSONObject("tags") ?: return null
        val name = tags.optString("name").takeIf { it.isNotBlank() } ?: return null

        val lat: Double
        val lon: Double
        if (element.has("lat") && element.has("lon")) {
            lat = element.getDouble("lat")
            lon = element.getDouble("lon")
        } else {
            val center = element.optJSONObject("center") ?: return null
            lat = center.getDouble("lat")
            lon = center.getDouble("lon")
        }

        val cuisineTag = tags.optString("cuisine").split(";").firstOrNull()?.trim()
        val cuisine = cuisineTag
            ?.replace("_", " ")
            ?.replaceFirstChar { it.uppercase(Locale.US) }
            ?.takeIf { it.isNotBlank() }
            ?: "Restaurant"

        val houseNumber = tags.optString("addr:housenumber")
        val street = tags.optString("addr:street")
        val address = when {
            houseNumber.isNotBlank() && street.isNotBlank() -> "$houseNumber $street"
            street.isNotBlank() -> street
            else -> tags.optString("addr:city").takeIf { it.isNotBlank() } ?: "Nearby"
        }

        // Occasionally a contributor adds a direct image URL; only use it
        // if it actually looks like an image file, not a Wikimedia page link.
        val imageTag = tags.optString("image")
        val photoUrl = imageTag.takeIf {
            it.startsWith("http") && Regex("\\.(jpe?g|png|webp)$", RegexOption.IGNORE_CASE).containsMatchIn(it)
        }

        return NearbyPlace(
            osmId = "${element.optString("type", "node")}/${element.optLong("id")}",
            name = name,
            cuisine = cuisine,
            address = address,
            latitude = lat,
            longitude = lon,
            photoUrl = photoUrl
        )
    }
}
