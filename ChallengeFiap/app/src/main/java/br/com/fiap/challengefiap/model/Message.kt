package br.com.fiap.challengefiap.model

data class Message(
    val text: String="",
    val timestamp: Long =0,
    val fromId: String="",
    val toId: String="",
    val senderName: String = "",
    val senderPhotoUrl: String = ""
)
