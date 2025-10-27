package br.com.fiap.challengefiap.model

data class Group(
    val id:String ="",
    val name: String = "",
    val createdBy : String ="",
    val member: List<String> = emptyList(),
    val createdAt:com.google.firebase.Timestamp?=null,
    val imageUrl: String? = null
)
