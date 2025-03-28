package com.nate.autofinance.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    suspend fun registerUser(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
    }
}
