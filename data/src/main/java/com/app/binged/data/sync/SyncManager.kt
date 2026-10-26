package com.app.binged.data.sync

import android.util.Log
import com.app.binged.data.database.AppDatabase
import com.app.binged.data.database.entity.EpisodeEntity
import com.app.binged.data.database.entity.ShowEntity
import com.app.binged.data.di.ApplicationScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: AppDatabase,
    @ApplicationScope private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "SyncManager"

        // Firestore allows 500 writes per batch; stay under it.
        private const val BATCH_WRITE_LIMIT = 400
    }

    private val uid: String?
        get() = auth.currentUser?.uid

    private val listeners = mutableListOf<ListenerRegistration>()

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
                                upsertFromRemote(change.document)
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
                                val rowId = change.document.id.toLongOrNull() ?: return@launch
                                database.episodeDao().deleteEpisodeByRowId(rowId)
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

    /**
     * Writes a remote show document into Room without clobbering local-only state.
     *
     * A document written by an older build simply has no `seenEpisodes` / `collapsedSeasons`
     * field. Blindly inserting the mapped entity would REPLACE the whole row and reset those
     * fields to their empty defaults, so any local value the push never delivered is lost.
     * Missing fields are therefore carried over from the existing local row instead.
     */
    private suspend fun upsertFromRemote(doc: DocumentSnapshot) {
        val remote = doc.toShowEntity() ?: return
        val hasSeen = doc.contains("seenEpisodes")
        val hasCollapsed = doc.contains("collapsedSeasons")
        if (hasSeen && hasCollapsed) {
            database.showDao().insertShow(remote)
            return
        }
        val local = database.showDao().getShowById(remote.id).first()
        database.showDao().insertShow(
            remote.copy(
                seenEpisodes = if (hasSeen) remote.seenEpisodes else local?.seenEpisodes.orEmpty(),
                collapsedSeasons = if (hasCollapsed) remote.collapsedSeasons else local?.collapsedSeasons.orEmpty()
            )
        )
    }

    suspend fun pullAll() {
        val user = uid ?: return
        val showsRef = firestore.collection("users").document(user).collection("trackedShows")
        val episodesRef = firestore.collection("users").document(user).collection("watchedEpisodes")

        try {
            val showsSnapshot = showsRef.get().await()
            for (doc in showsSnapshot.documents) {
                upsertFromRemote(doc)
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

    fun deleteEpisode(id: Long) {
        val user = uid ?: return
        scope.launch {
            try {
                firestore.collection("users").document(user)
                    .collection("watchedEpisodes")
                    .document(id.toString())
                    .delete()
            } catch (e: Exception) { Log.e(TAG, "deleteEpisode failed for id=$id", e) }
        }
    }

    /**
     * Deletes every document this user owns in Firestore. Throws when the user is
     * not signed in or when a commit fails, so callers can abort instead of
     * removing the account while its cloud data survives.
     */
    suspend fun deleteAll() {
        val user = uid ?: error("Cannot delete cloud data without a signed-in user")
        val userRef = firestore.collection("users").document(user)
        deleteCollection(userRef.collection("trackedShows"))
        deleteCollection(userRef.collection("watchedEpisodes"))
    }

    private suspend fun deleteCollection(collection: CollectionReference) {
        val documents = collection.get().await().documents
        documents.chunked(BATCH_WRITE_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
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
    "isWatching" to isWatching,
    "seenEpisodes" to seenEpisodes.toList(),
    "collapsedSeasons" to collapsedSeasons.toList()
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
    "notes" to notes,
    "runtime" to runtime.toLong(),
    "isRewatch" to isRewatch,
    "rating" to rating
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
        isWatching = getBoolean("isWatching") ?: false,
        seenEpisodes = (get("seenEpisodes") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        collapsedSeasons = (get("collapsedSeasons") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet() ?: emptySet()
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
        notes = getString("notes"),
        runtime = (getLong("runtime") ?: 0L).toInt(),
        isRewatch = getBoolean("isRewatch") ?: false,
        rating = (getLong("rating") ?: getDouble("rating")?.toLong())?.toInt()
    )
}
