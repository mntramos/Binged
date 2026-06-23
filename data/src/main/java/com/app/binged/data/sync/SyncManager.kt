package com.app.binged.data.sync

import android.util.Log
import com.app.binged.data.database.AppDatabase
import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: AppDatabase
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    private val uid: String?
        get() = auth.currentUser?.uid

    private val listeners = mutableListOf<ListenerRegistration>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startListening() {
        val user = uid ?: return
        stopListening()

        val showsRef = firestore.collection("users").document(user).collection("trackedShows")
        val episodesRef = firestore.collection("users").document(user).collection("watchedEpisodes")

        listeners.add(
            showsRef.addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    scope.launch {
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = change.document.toShowEntity() ?: return@launch
                                database.showDao().insertShow(entity)
                            }
                            DocumentChange.Type.REMOVED -> {
                                val id = change.document.getLong("id")?.toInt() ?: return@launch
                                database.showDao().deleteShowById(id)
                                database.episodeDao().deleteEpisodesByShow(id)
                            }
                        }
                    }
                }
            }
        )

        listeners.add(
            episodesRef.addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    scope.launch {
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                val entity = change.document.toEpisodeEntity() ?: return@launch
                                database.episodeDao().insertEpisode(entity)
                            }
                            DocumentChange.Type.REMOVED -> {
                                val showId = change.document.getLong("showId")?.toInt() ?: return@launch
                                val episodeId = change.document.getLong("episodeId")?.toInt() ?: return@launch
                                database.episodeDao().deleteEpisodeById(episodeId, showId)
                            }
                        }
                    }
                }
            }
        )
    }

    fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    suspend fun pullAll() {
        val user = uid ?: return
        val showsRef = firestore.collection("users").document(user).collection("trackedShows")
        val episodesRef = firestore.collection("users").document(user).collection("watchedEpisodes")

        try {
            val showsSnapshot = showsRef.get().await()
            for (doc in showsSnapshot.documents) {
                val entity = doc.toShowEntity() ?: continue
                database.showDao().insertShow(entity)
            }
        } catch (e: Exception) { Log.e(TAG, "pullAll shows failed", e) }

        try {
            val episodesSnapshot = episodesRef.get().await()
            for (doc in episodesSnapshot.documents) {
                val entity = doc.toEpisodeEntity() ?: continue
                database.episodeDao().insertEpisode(entity)
            }
        } catch (e: Exception) { Log.e(TAG, "pullAll episodes failed", e) }
    }

    fun pushShow(show: ShowEntity) {
        val user = uid ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user)
                    .collection("trackedShows")
                    .document(show.id.toString())
                    .set(show.toMap())
            } catch (e: Exception) { Log.e(TAG, "pushShow failed for showId=${show.id}", e) }
        }
    }

    fun pushEpisode(episode: EpisodeEntity) {
        val user = uid ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user)
                    .collection("watchedEpisodes")
                    .document(episode.id.toString())
                    .set(episode.toMap())
            } catch (e: Exception) { Log.e(TAG, "pushEpisode failed for episodeId=${episode.id}", e) }
        }
    }

    fun deleteShow(showId: Int) {
        val user = uid ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user)
                    .collection("trackedShows")
                    .document(showId.toString())
                    .delete()
            } catch (e: Exception) { Log.e(TAG, "deleteShow failed for showId=$showId", e) }
        }
    }

    fun deleteEpisode(episodeId: Int, showId: Int) {
        val user = uid ?: return
        scope.launch {
            try {
                val episodesRef = firestore.collection("users").document(user)
                    .collection("watchedEpisodes")
                val snapshot = episodesRef.whereEqualTo("episodeId", episodeId.toLong())
                    .whereEqualTo("showId", showId.toLong())
                    .get()
                    .await()
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            } catch (e: Exception) { Log.e(TAG, "deleteEpisode failed for episodeId=$episodeId showId=$showId", e) }
        }
    }

    suspend fun deleteAll() {
        val user = uid ?: return
        try {
            val showsRef = firestore.collection("users").document(user).collection("trackedShows")
            val episodesRef = firestore.collection("users").document(user).collection("watchedEpisodes")

            for (doc in showsRef.get().await().documents) {
                doc.reference.delete()
            }
            for (doc in episodesRef.get().await().documents) {
                doc.reference.delete()
            }
        } catch (e: Exception) { Log.e(TAG, "deleteAll failed", e) }
    }
}

private fun ShowEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id.toLong(),
    "name" to name,
    "overview" to overview,
    "backdropPath" to backdropPath,
    "posterPath" to posterPath,
    "firstAirDate" to firstAirDate,
    "rating" to rating,
    "tagline" to tagline,
    "seasonCount" to seasonCount.toLong(),
    "isFavorite" to isFavorite,
    "isWatching" to isWatching
)

private fun EpisodeEntity.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "episodeId" to episodeId.toLong(),
    "showId" to showId.toLong(),
    "showName" to showName,
    "seasonNumber" to seasonNumber.toLong(),
    "episodeNumber" to episodeNumber.toLong(),
    "title" to title,
    "watchedDate" to watchedDate,
    "stillPath" to stillPath,
    "notes" to notes
)

private fun DocumentSnapshot.toShowEntity(): ShowEntity? {
    val id = getLong("id")?.toInt() ?: return null
    return ShowEntity(
        id = id,
        name = getString("name") ?: "",
        overview = getString("overview") ?: "",
        backdropPath = getString("backdropPath"),
        posterPath = getString("posterPath"),
        firstAirDate = getString("firstAirDate") ?: "",
        rating = getDouble("rating") ?: 0.0,
        tagline = getString("tagline") ?: "",
        seasonCount = (getLong("seasonCount") ?: 1L).toInt(),
        isFavorite = getBoolean("isFavorite") ?: false,
        isWatching = getBoolean("isWatching") ?: false
    )
}

private fun DocumentSnapshot.toEpisodeEntity(): EpisodeEntity? {
    val id = getLong("id") ?: return null
    return EpisodeEntity(
        id = id,
        episodeId = (getLong("episodeId") ?: return null).toInt(),
        showId = (getLong("showId") ?: return null).toInt(),
        showName = getString("showName") ?: "",
        seasonNumber = (getLong("seasonNumber") ?: return null).toInt(),
        episodeNumber = (getLong("episodeNumber") ?: return null).toInt(),
        title = getString("title") ?: "",
        watchedDate = getLong("watchedDate") ?: 0L,
        stillPath = getString("stillPath"),
        notes = getString("notes")
    )
}
