package com.jakebarnby.filemanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.ui.adapters.SourceLogoutAdapter.LogoutViewHolder
import com.jakebarnby.filemanager.util.Utils

/**
 * Created by Jake on 8/6/2017.
 */
class SourceLogoutAdapter(
    private val sources: MutableList<Source>,
    private val listener: LogoutListener
) : RecyclerView.Adapter<LogoutViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_logout_list, parent, false)
        return LogoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogoutViewHolder, position: Int) {
        holder.bindHolder(sources, position)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    inner class LogoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val logo: ImageView = itemView.findViewById(R.id.img_logo_logout)
        private val sourceName: TextView = itemView.findViewById(R.id.txt_source_title)
        private val logout: Button = itemView.findViewById(R.id.btn_logout)

        fun bindHolder(sources: MutableList<Source>, position: Int) {

            val source = sources[position]

            logo.setImageResource(Utils.resolveLogoId(source.sourceName))
            sourceName.text = String.format("%s", source.sourceName)
            logout.setText(if (source.isLoggedIn) R.string.logout else R.string.connect)
            logout.setOnClickListener { view: View ->
                if (!source.isLoggedIn) {
                    source.authenticate(logo.context)
                    return@setOnClickListener
                }

                source.logout(view.context)
                sources.remove(source)
                notifyDataSetChanged()
                if (sources.size == 0) {
                    listener.onLastLogout()
                }
            }
        }

    }

    @FunctionalInterface
    interface LogoutListener {
        fun onLastLogout()
    }

}