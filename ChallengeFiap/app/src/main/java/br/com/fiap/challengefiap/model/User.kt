package br.com.fiap.challengefiap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val url: String? ="",
    val tipo: String ="",
    val fcmToken: String? = null
): Parcelable
