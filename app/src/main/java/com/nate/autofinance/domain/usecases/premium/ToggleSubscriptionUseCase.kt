package com.nate.autofinance.domain.usecases.subscription

import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.domain.models.User
import com.nate.autofinance.utils.SessionManager
import android.content.Context

class ToggleSubscriptionUseCase(
    private val userRepo: UserRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(subscribe: Boolean) {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val user = userRepo.getUserById(userId)
            ?: throw IllegalStateException("Usuário com id $userId não encontrado")

        val updated = user.copy(isSubscribed = subscribe)
        userRepo.updateUser(updated)
    }
}

