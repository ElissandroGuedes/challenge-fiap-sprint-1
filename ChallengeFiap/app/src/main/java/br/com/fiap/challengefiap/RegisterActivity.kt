package br.com.fiap.challengefiap

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.challengefiap.databinding.ActivityRegisterBinding
import br.com.fiap.challengefiap.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private var selecteUri : Uri? = null

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            createUser()
        }

        binding.btnAddPhoto.setOnClickListener {
            getContent.launch("image/*")
        }

    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){
        uri : Uri? -> Log.i("Teste", "Uri selecionada : ${uri.toString()}")
        selecteUri = uri
        try {
            uri?.let{
                if (Build.VERSION.SDK_INT < 28){
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,it)
                } else{
                    val source = ImageDecoder.createSource(this.contentResolver, it)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    binding.imgPhoto.setImageBitmap(bitmap)
                }
            }

        binding.btnAddPhoto.alpha = 0.0f
        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    private fun createUser(){
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(
                this,
                "email e senha devem ser informados",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    saveUserInFirebase()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {  }
    }

    private fun saveUserInFirebase() {
        val uri = selecteUri
        val name = binding.editName.text.toString()
        val uid = FirebaseAuth.getInstance().uid ?: return
        val tipoSelecionado = when (binding.radioGroupTipo.checkedRadioButtonId) {
            R.id.radioOperador -> "operador"
            else -> "cliente"
        }

        if (uri == null) {
            Toast.makeText(this, "Selecione uma foto primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        Log.i("Storage", "URL da imagem: $downloadUri")

                        val user = User(uid = uid, name = name, url = downloadUri.toString(),tipoSelecionado)

                        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Usuário salvo com sucesso")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Erro ao salvar usuário", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Storage", "Falha ao obter downloadUrl", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Storage", "Falha no upload da imagem", e)
                Toast.makeText(this, "Falha no upload: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}