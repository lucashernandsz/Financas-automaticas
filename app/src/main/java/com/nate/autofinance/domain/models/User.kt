package com.nate.autofinance.domain.models

import com.nate.autofinance.domain.models.SyncStatus
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isSubscribed: Boolean = false,
    var firebaseDocId: String? = null
) {
    /** Construtor sem argumentos exigido pelo Firestore via reflexão */
    @Suppress("unused")
    constructor() : this(
        id             = 0,
        name           = "",
        email          = "",
        syncStatus     = SyncStatus.PENDING,
        isSubscribed   = false,
        firebaseDocId  = null
    )
}
