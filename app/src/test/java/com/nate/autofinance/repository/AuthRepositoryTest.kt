package com.nate.autofinance.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        // Cria um mock do FirebaseAuth
        firebaseAuth = Mockito.mock()
        authRepository = AuthRepository(firebaseAuth)
    }

    @Test
    fun `registerUser returns FirebaseUser on success`() = runBlocking {
        val fakeUser: FirebaseUser = Mockito.mock()
        val fakeAuthResult: AuthResult = Mockito.mock()

        val fakeTask = Tasks.forResult(fakeAuthResult)
        whenever(fakeAuthResult.user).thenReturn(fakeUser)
        whenever(firebaseAuth.createUserWithEmailAndPassword("teste@exemplo.com", "senha123"))
            .thenReturn(fakeTask)

        val resultUser = authRepository.registerUser("teste@exemplo.com", "senha123")

        Assert.assertNotNull(resultUser)
        Assert.assertEquals(fakeUser, resultUser)
    }

    @Test
    fun `loginUser returns FirebaseUser on success`() = runBlocking {
        val fakeUser: FirebaseUser = Mockito.mock()
        val fakeAuthResult: AuthResult = Mockito.mock()

        val fakeTask = Tasks.forResult(fakeAuthResult)
        whenever(fakeAuthResult.user).thenReturn(fakeUser)
        whenever(firebaseAuth.signInWithEmailAndPassword("teste@exemplo.com", "senha123"))
            .thenReturn(fakeTask)

        val resultUser = authRepository.loginUser("teste@exemplo.com", "senha123")

        Assert.assertNotNull(resultUser)
        Assert.assertEquals(fakeUser, resultUser)
    }
}