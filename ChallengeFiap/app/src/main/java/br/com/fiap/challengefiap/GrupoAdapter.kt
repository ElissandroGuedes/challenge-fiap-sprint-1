package br.com.fiap.challengefiap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fiap.challengefiap.model.Group
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class GrupoAdapter(
    private val grupos: List<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder>() {

    inner class GrupoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageGrupo: ImageView = itemView.findViewById(R.id.imageGrupo)
        val textNomeGrupo: TextView = itemView.findViewById(R.id.textNomeGrupo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grupo, parent, false)
        return GrupoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        val grupo = grupos[position]
        holder.textNomeGrupo.text = grupo.name

        Glide.with(holder.itemView.context)
            .load(grupo.imageUrl)
            .transform(CenterCrop(), RoundedCorners(16))
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.imageGrupo)

        holder.itemView.setOnClickListener {
            onClick(grupo)
        }
    }

    override fun getItemCount(): Int = grupos.size
}