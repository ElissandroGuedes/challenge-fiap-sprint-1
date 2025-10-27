package br.com.fiap.challengefiap.model

import android.text.LoginFilter

data class Contact(
    val uuid:String ="",
    val userName:String ="",
    val lastMessage: String ="",
    val photoUrl:String="",
    val timestamp: Long =0,
    val isGroup: Boolean = false
)
