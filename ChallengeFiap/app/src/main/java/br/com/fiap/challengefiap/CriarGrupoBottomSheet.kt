package br.com.fiap.challengefiap

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.model.Group
import br.com.fiap.challengefiap.model.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CriarGrupoBottomSheet : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var clienteAdapter: ClienteAdapter
    private val selecionados = mutableSetOf<String>()
    private val listaDeClientes = mutableListOf<User>()
    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagemLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagemSelecionadaUri = it
            val imageView = view?.findViewById<ImageView>(R.id.imageGrupo)
            imageView?.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_criar_grupo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editNomeGrupo = view.findViewById<TextInputEditText>(R.id.editNomeGrupo)
        val btnSalvarGrupo = view.findViewById<MaterialButton>(R.id.btnSalvarGrupo)
        val recyclerClientes = view.findViewById<RecyclerView>(R.id.recyclerClientes)
        val btnEscolherImagem = view.findViewById<MaterialButton>(R.id.btnEscolherImagem)
        val operadorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Configura RecyclerView
        clienteAdapter = ClienteAdapter(listaDeClientes, selecionados)
        recyclerClientes.adapter = clienteAdapter
        recyclerClientes.layoutManager = LinearLayoutManager(context)

        // Carrega os clientes do Firestore
        db.collection("users").get().addOnSuccessListener { result ->
            val clientes = result.toObjects(User::class.java)
            listaDeClientes.clear()
            listaDeClientes.addAll(clientes)
            clienteAdapter.notifyDataSetChanged()
        }

        // Escolher imagem
        btnEscolherImagem.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        // Salvar grupo
        btnSalvarGrupo.setOnClickListener {
            val nomeGrupo = editNomeGrupo.text.toString().trim()
            val grupoRef = db.collection("groups").document()
            val grupoId = grupoRef.id

            if (nomeGrupo.isEmpty()) {
                Toast.makeText(context, "Digite um nome para o grupo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selecionados.isEmpty()) {
                Toast.makeText(context, "Selecione pelo menos um cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val grupoBase = Group(
                id = grupoId,
                name = nomeGrupo,
                createdBy = operadorId,
                member = selecionados.toList(),
                createdAt = Timestamp.now()
            )

            if (imagemSelecionadaUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/grupos/$grupoId.jpg")

                imageRef.putFile(imagemSelecionadaUri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val grupoComImagem = grupoBase.copy(imageUrl = downloadUrl.toString())
                            salvarGrupo(grupoRef, grupoComImagem)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show()
                    }
            } else {
                salvarGrupo(grupoRef, grupoBase)
            }
        }
    }

    private fun salvarGrupo(ref: DocumentReference, grupo: Group) {
        ref.set(grupo)
            .addOnSuccessListener {
                Toast.makeText(context, "Grupo criado com sucesso!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao criar grupo", Toast.LENGTH_SHORT).show()
            }
    }
}