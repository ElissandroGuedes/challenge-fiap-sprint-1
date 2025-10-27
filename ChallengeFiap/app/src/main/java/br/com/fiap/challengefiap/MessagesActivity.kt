package br.com.fiap.challengefiap

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.databinding.ActivityMessagesBinding
import br.com.fiap.challengefiap.model.Contact
import br.com.fiap.challengefiap.model.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        verifyAuthentication()

        adapter = GroupAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.list_messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchLastMessages()
    }

    private fun verifyAuthentication() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun fetchLastMessages() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        FirebaseFirestore.getInstance().collection("last-messages")
            .document(uid)
            .collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED || doc.type == DocumentChange.Type.MODIFIED) {
                        val contact = doc.document.toObject(Contact::class.java)
                        adapter.add(ContactItem(contact))

                    }
                }
            }
    }

    private inner class ContactItem(private val contact: Contact) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int = R.layout.item_user_message

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val nameText = viewHolder.itemView.findViewById<TextView>(R.id.txt_username)
            val messageText = viewHolder.itemView.findViewById<TextView>(R.id.txt_last_message)
            val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.img_foto)

            nameText.text = contact.userName
            messageText.text = contact.lastMessage

            if (!contact.photoUrl.isNullOrBlank()) {
                Glide.with(viewHolder.itemView.context)
                    .load(contact.photoUrl)
                    .placeholder(
                        if (contact.isGroup) R.drawable.ic_group else R.drawable.ic_default_avatar
                    )
                    .error(
                        if (contact.isGroup) R.drawable.ic_group else R.drawable.ic_default_avatar
                    )
                    .into(imageView)
            } else {
                imageView.setImageResource(
                    if (contact.isGroup) R.drawable.ic_group else R.drawable.ic_default_avatar
                )
            }
            viewHolder.itemView.setOnClickListener {
                val intent = Intent(it.context, ChatActivity::class.java)
                if (contact.isGroup == true) {
                    intent.putExtra("groupId", contact.uuid)
                    intent.putExtra("groupName", contact.userName)
                } else {
                    // Se quiser abrir conversa individual, você pode buscar o User completo
                    val user = User(
                        uid = contact.uuid,
                        name = contact.userName,
                        url = contact.photoUrl
                    )
                    intent.putExtra("user_key", user)
                }
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                verifyAuthentication()
            }
            R.id.contacts -> {
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}