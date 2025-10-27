package br.com.fiap.challengefiap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.challengefiap.model.Campaign
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SendCampaignActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_campaing)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_contato -> {

                    val intent = Intent(this, ContactsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_sair -> {

                    FirebaseAuth.getInstance().signOut()


                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val btnCriarGrupo = findViewById<Button>(R.id.btnCriarGrupo)
        btnCriarGrupo.setOnClickListener {
            val dialog = CriarGrupoBottomSheet()
            dialog.show(supportFragmentManager, "CriarGrupo")
        }

        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val title = findViewById<EditText>(R.id.editTitle).text.toString()
            val body = findViewById<EditText>(R.id.editBody).text.toString()
            val url = findViewById<EditText>(R.id.editUrl).text.toString()
            val btn1 = findViewById<EditText>(R.id.editBtn1).text.toString()
            val btn1Url = findViewById<EditText>(R.id.editBtn1Url).text.toString()

            val campaign = Campaign(
                title, body, url,  listOf(mapOf("action" to "btn1", "title" to btn1))
                , mapOf("btn1" to btn1Url, "abrir" to url)
                , FirebaseAuth.getInstance().uid, com.google.firebase.Timestamp.now()
            )

            FirebaseFirestore.getInstance().collection("campaigns").add(campaign.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, "Campanha enviada!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao enviar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}