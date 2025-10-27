package br.com.fiap.challengefiap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.model.Contact
import br.com.fiap.challengefiap.model.Message
import br.com.fiap.challengefiap.model.User
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatActivity : AppCompatActivity() {
    private lateinit var mAdapter: GroupAdapter<GroupieViewHolder>
    private var user: User? = null
    private var me: User? = null
    private var groupId: String? = null
    private var groupName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        user = intent.getParcelableExtra("user_key")
        groupId = intent.getStringExtra("groupId")
        groupName = intent.getStringExtra("groupName")

        val nameTextView = findViewById<TextView>(R.id.text_contact_name_toolbar)
        val imageView =
            findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.image_contact_toolbar)
        val goBack = findViewById<MaterialToolbar>(R.id.topAppBar)

        goBack.setNavigationOnClickListener { finish() }

        if (!groupId.isNullOrEmpty()) {
            nameTextView.text = groupName ?: "Grupo"

            FirebaseFirestore.getInstance().collection("groups")
                .document(groupId!!)
                .get()
                .addOnSuccessListener { doc ->
                    val group = doc.toObject(br.com.fiap.challengefiap.model.Group::class.java)
                    val imageUrl = group?.imageUrl

                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_group)
                        .error(R.drawable.ic_group)
                        .into(imageView)
                }
                .addOnFailureListener {
                    Log.e("ChatActivity", "Erro ao buscar imagem do grupo", it)
                    imageView.setImageResource(R.drawable.ic_group)
                }

        } else {
            nameTextView.text = user?.name ?: "Contato"

            Glide.with(this)
                .load(user?.url)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(imageView)
        }

        mAdapter = GroupAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.list_chat)
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btn_send).setOnClickListener {
            sendMessage()
        }

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            Log.e("ChatActivity", "Usuário não autenticado")
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                me = document.toObject(User::class.java)
                Log.d("ChatActivity", "Usuário atual: ${me?.name}")
                if (me != null) {
                    fetchMessages()
                }
            }
            .addOnFailureListener {
                Log.e("ChatActivity", "Erro ao buscar usuário atual", it)
            }
    }

    private fun fetchMessages() {
        val db = FirebaseFirestore.getInstance()

        if (!groupId.isNullOrEmpty()) {
            Log.d("ChatActivity", "Listener ativado para grupo: $groupId")

            db.collection("groups")
                .document(groupId!!)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatActivity", "Erro ao escutar mensagens do grupo", error)
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val message = change.document.toObject(Message::class.java)
                            Log.d("ChatActivity", "Mensagem recebida: ${message.text}")
                            mAdapter.add(MessageItem(message))
                            findViewById<RecyclerView>(R.id.list_chat).scrollToPosition(mAdapter.itemCount - 1)
                        }
                    }
                }
        } else {
            val fromId = me?.uid ?: return
            val toId = user?.uid ?: return

            Log.d("ChatActivity", "Listener ativado para conversa entre $fromId e $toId")

            db.collection("conversations")
                .document(fromId)
                .collection(toId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatActivity", "Erro ao escutar mensagens individuais", error)
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val message = change.document.toObject(Message::class.java)
                            Log.d("ChatActivity", "Mensagem recebida: ${message.text}")
                            mAdapter.add(MessageItem(message))
                            findViewById<RecyclerView>(R.id.list_chat).scrollToPosition(mAdapter.itemCount - 1)
                        }
                    }
                }
        }
    }

    private fun sendMessage() {
        val editText = findViewById<EditText>(R.id.edit_msg)
        val text = editText.text.toString().trim()
        editText.text = null

        if (text.isEmpty()) {
            Log.w("ChatActivity", "Mensagem vazia não enviada")
            return
        }

        val fromId = FirebaseAuth.getInstance().uid
        if (fromId.isNullOrEmpty()) {
            Log.e("ChatActivity", "fromId está nulo")
            return
        }

        val timestamp = System.currentTimeMillis()

        val isGroupChat = !groupId.isNullOrBlank()
        val toId = if (isGroupChat) groupId else user?.uid

        if (toId.isNullOrBlank()) {
            Log.e(
                "ChatActivity",
                "toId está nulo — verifique se user ou groupId foi passado corretamente"
            )
            return
        }

        val message = Message(
            text = text,
            timestamp = timestamp,
            toId = toId,
            fromId = fromId
        )

        val db = FirebaseFirestore.getInstance()

        if (isGroupChat) {
            Log.d("ChatActivity", "Enviando mensagem para grupo $groupId: $text")

            db.collection("groups")
                .document(groupId!!)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    Log.d("ChatActivity", "Mensagem enviada ao grupo com sucesso")

                    db.collection("groups")
                        .document(groupId!!)
                        .get()
                        .addOnSuccessListener { doc ->
                            val group =
                                doc.toObject(br.com.fiap.challengefiap.model.Group::class.java)
                            val contact = Contact(
                                uuid = groupId!!,
                                userName = group?.name ?: "Grupo",
                                lastMessage = text,
                                photoUrl = group?.imageUrl ?: "",
                                timestamp = timestamp,
                                isGroup = true
                            )
                            db.collection("last-messages")
                                .document(fromId)
                                .collection("contacts")
                                .document(groupId!!)
                                .set(contact)
                                .addOnSuccessListener {
                                    Log.d(
                                        "ChatActivity",
                                        "Última mensagem do grupo salva com sucesso"
                                    )
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "ChatActivity",
                                        "Erro ao salvar última mensagem do grupo",
                                        it
                                    )
                                }
                        }
                        .addOnFailureListener {
                            Log.e("ChatActivity", "Erro ao enviar mensagem para grupo", it)
                        }
                }

        } else {
            Log.d("ChatActivity", "Enviando mensagem para usuário $toId: $text")

            db.collection("conversations")
                .document(fromId)
                .collection(toId)
                .add(message)
                .addOnSuccessListener {
                    Log.d("ChatActivity", "Mensagem enviada para $toId com sucesso")

                    val contact = Contact(
                        uuid = toId,
                        userName = user?.name ?: "",
                        lastMessage = text,
                        photoUrl = user?.url ?: "",
                        timestamp = timestamp,
                        isGroup = false
                    )

                    db.collection("last-messages")
                        .document(fromId)
                        .collection("contacts")
                        .document(toId)
                        .set(contact)
                        .addOnSuccessListener {
                            Log.d("ChatActivity", "Última mensagem salva para $toId")
                        }
                        .addOnFailureListener {
                            Log.e("ChatActivity", "Erro ao salvar última mensagem para $toId", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("ChatActivity", "Erro ao enviar mensagem para $toId", it)
                }

            db.collection("conversations")
                .document(toId)
                .collection(fromId)
                .add(message)
                .addOnSuccessListener {
                    val contact = Contact(
                        uuid = fromId,
                        userName = me?.name ?: "",
                        lastMessage = text,
                        photoUrl = me?.url ?: "",
                        timestamp = timestamp,
                        isGroup = false
                    )

                    db.collection("last-messages")
                        .document(toId)
                        .collection("contacts")
                        .document(fromId)
                        .set(contact)
                        .addOnSuccessListener {
                            Log.d("ChatActivity", "Última mensagem salva para $fromId")
                        }
                        .addOnFailureListener {
                            Log.e("ChatActivity", "Erro ao salvar última mensagem para $fromId", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("ChatActivity", "Erro ao enviar mensagem reversa para $fromId", it)
                }
        }
    }

    private inner class MessageItem(private val message: Message) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return if (message.fromId == FirebaseAuth.getInstance().uid)
                R.layout.item_from_message
            else
                R.layout.item_to_message
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val textView = if (message.fromId == FirebaseAuth.getInstance().uid)
                viewHolder.itemView.findViewById<TextView>(R.id.txt_msg_from)
            else
                viewHolder.itemView.findViewById<TextView>(R.id.txt_msg)

            textView.text = message.text

            val imageView = if (message.fromId == FirebaseAuth.getInstance().uid)
                viewHolder.itemView.findViewById<ImageView>(R.id.img_msg_from)
            else
                viewHolder.itemView.findViewById<ImageView>(R.id.img_msg)

            val imageUrl = if (message.fromId == FirebaseAuth.getInstance().uid)
                me?.url
            else if (!groupId.isNullOrEmpty())
                null
            else
                user?.url

            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl).into(imageView)
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher)
            }
        }
    }
}