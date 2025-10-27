package br.com.fiap.challengefiap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.model.Group
import br.com.fiap.challengefiap.model.User
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ContactsActivity : AppCompatActivity() {

    private lateinit var adapter: GroupAdapter<GroupieViewHolder>
    private val db = FirebaseFirestore.getInstance()
    private val currentUid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val goBack = findViewById<MaterialToolbar>(R.id.topAppBar)
        goBack.setNavigationOnClickListener { finish() }

        adapter = GroupAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.list_contact)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this@ContactsActivity, ChatActivity::class.java)
            when (item) {
                is UserItem -> intent.putExtra("user_key", item.user)
                is GroupItem -> {
                    intent.putExtra("groupId", item.group.id)
                    intent.putExtra("groupName", item.group.name)
                }
            }
            startActivity(intent)
        }

        val searchEditText = findViewById<EditText>(R.id.edit_search)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterContacts(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchUsersAndGroups()
    }

    private val allItems = mutableListOf<Item<GroupieViewHolder>>()

    private fun fetchUsersAndGroups() {
        allItems.clear()

        db.collection("users").get().addOnSuccessListener { userSnapshot ->
            val users = userSnapshot.toObjects(User::class.java)
            users.filter { it.uid != currentUid }.forEach {
                allItems.add(UserItem(it))
            }

            db.collection("groups").get().addOnSuccessListener { groupSnapshot ->
                val groups = groupSnapshot.toObjects(Group::class.java)
                groups.forEach {
                    allItems.add(GroupItem(it))
                }

                adapter.update(allItems)
            }
        }
    }

    private fun filterContacts(query: String) {
        val filtered = allItems.filter {
            when (it) {
                is UserItem -> it.user.name.contains(query, ignoreCase = true)
                is GroupItem -> it.group.name.contains(query, ignoreCase = true)
                else -> false
            }
        }

        adapter.update(filtered)
    }

    private inner class UserItem(val user: User) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.item_contact
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.txt_username).text = user.name
            val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.imgFoto)
            Picasso.get().load(user.url).into(imageView)
        }
    }

    private inner class GroupItem(val group: Group) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.item_contact

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val nameText = viewHolder.itemView.findViewById<TextView>(R.id.txt_username)
            val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.imgFoto)

            nameText.text = group.name

            val imageUrl = group.imageUrl
            val placeholder = R.drawable.ic_group

            if (!imageUrl.isNullOrBlank()) {
                Glide.with(viewHolder.itemView.context)
                    .load(imageUrl)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(placeholder)
            }
        }
    }
}