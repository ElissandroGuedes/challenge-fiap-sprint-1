package br.com.fiap.challengefiap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.challengefiap.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Campanhas",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }


        binding.btnEnter.setOnClickListener {
            signIn()
        }

        binding.txtAcount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun signIn() {
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        val db = FirebaseFirestore.getInstance()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email e senha devem ser informados", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                    val uid = FirebaseAuth.getInstance().uid ?: return@addOnCompleteListener


                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result
                            val userRef = db.collection("users").document(uid)
                            userRef.update("fcmToken", token)
                                .addOnSuccessListener {
                                    Log.d("FCM", "Token salvo com sucesso")
                                }
                                .addOnFailureListener {
                                    Log.e("FCM", "Erro ao salvar token", it)
                                }
                        } else {
                            Log.e("FCM", "Erro ao obter token", tokenTask.exception)
                        }
                    }

                    // Verifica o tipo de usuário e redireciona
                    db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                        val tipo = doc.getString("tipo")
                        val intent = if (tipo == "operador") {
                            Intent(this@LoginActivity, SendCampaignActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, MessagesActivity::class.java)
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Login", it.message, it)
                Toast.makeText(this, "Erro ao fazer login", Toast.LENGTH_SHORT).show()
            }
    }


}