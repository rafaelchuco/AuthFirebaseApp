package com.tecsup.authfirebaseapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.authfirebaseapp.domain.model.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // CREATE
    suspend fun createEvent(event: Event): Result<Unit> = try {
        db.collection("events")
            .document(event.id)
            .set(event)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // READ (Con snapshot listener para tiempo real)
    fun getEventsByUserRealtime(userId: String): Flow<List<Event>> = callbackFlow {
        val listener = db.collection("events")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.map { doc ->
                    Event(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        date = doc.getString("date") ?: "",
                        description = doc.getString("description") ?: "",
                        userId = doc.getString("userId") ?: ""
                    )
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }

    // READ (Sin tiempo real)
    suspend fun getEventsByUser(userId: String): Result<List<Event>> = try {
        val snapshot = db.collection("events")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        
        val events = snapshot.documents.map { doc ->
            Event(
                id = doc.getString("id") ?: doc.id,
                title = doc.getString("title") ?: "",
                date = doc.getString("date") ?: "",
                description = doc.getString("description") ?: "",
                userId = doc.getString("userId") ?: ""
            )
        }
        Result.success(events)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // UPDATE
    suspend fun updateEvent(eventId: String, updates: Map<String, Any>): Result<Unit> = try {
        db.collection("events")
            .document(eventId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // DELETE
    suspend fun deleteEvent(eventId: String): Result<Unit> = try {
        db.collection("events")
            .document(eventId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
