package br.com.fiap.challengefiap.model

data class Campaign(
    val title:String="",
    val body: String="",
    val url: String="",
    val actions:List<Map<String, String>>?=null,
    val actionUrls:Map<String, String>?=null,
    val createdBy:String?=null,
    val createdAt:com.google.firebase.Timestamp?=null
) : java.io.Serializable{
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "body" to body,
            "url" to url,
            "actions" to actions,
            "actionUrls" to actionUrls,
            "createdBy" to createdBy,
            "createdAt" to createdAt
        )
    }
}


