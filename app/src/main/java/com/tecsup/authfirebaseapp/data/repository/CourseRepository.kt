package com.tecsup.authfirebaseapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.authfirebaseapp.domain.model.Course
import kotlinx.coroutines.tasks.await

class CourseRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // CREATE
    suspend fun createCourse(course: Course): Result<Unit> = try {
        db.collection("courses")
            .document(course.id)
            .set(course)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // READ
    suspend fun getCoursesByUser(userId: String): Result<List<Course>> = try {
        val snapshot = db.collection("courses")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        
        val courses = snapshot.documents.map { doc ->
            Course(
                id = doc.getString("id") ?: doc.id,
                name = doc.getString("name") ?: "",
                teacher = doc.getString("teacher") ?: "",
                credits = (doc.getLong("credits") ?: 0L).toInt(),
                userId = doc.getString("userId") ?: ""
            )
        }
        Result.success(courses)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // UPDATE
    suspend fun updateCourse(courseId: String, updates: Map<String, Any>): Result<Unit> = try {
        db.collection("courses")
            .document(courseId)
            .update(updates)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // DELETE
    suspend fun deleteCourse(courseId: String): Result<Unit> = try {
        db.collection("courses")
            .document(courseId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
