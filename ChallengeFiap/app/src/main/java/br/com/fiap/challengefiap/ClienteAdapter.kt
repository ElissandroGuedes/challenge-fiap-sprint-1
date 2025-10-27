package br.com.fiap.challengefiap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.model.User

class ClienteAdapter(
    private val clientes: List<User>,
    private val selecionados: MutableSet<String>
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome = itemView.findViewById<TextView>(R.id.textNomeCliente)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkboxCliente)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente_selecao, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.nome.text = cliente.name
        holder.checkbox.isChecked = selecionados.contains(cliente.uid)

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selecionados.add(cliente.uid)
            } else {
                selecionados.remove(cliente.uid)
            }
        }
    }

    override fun getItemCount(): Int = clientes.size
}