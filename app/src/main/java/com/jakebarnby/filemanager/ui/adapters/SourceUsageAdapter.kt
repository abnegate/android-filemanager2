package com.jakebarnby.filemanager.ui.adapters

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dinuscxj.progressbar.CircleProgressBar
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.ui.adapters.SourceUsageAdapter.UsageViewHolder
import com.jakebarnby.filemanager.util.Constants
import java.util.*

/**
 * Created by Jake on 7/31/2017.
 */
class SourceUsageAdapter(
    private val sources: List<Source>
) : RecyclerView.Adapter<UsageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_usage_list, parent, false)

        return UsageViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UsageViewHolder, position: Int) {
        holder.bindHolder(sources[position])
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    class UsageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val percentBar: CircleProgressBar = itemView.findViewById(R.id.prg_usage)
        private val sourceName: TextView = itemView.findViewById(R.id.txt_source_title)
        private val sourceUsage: TextView = itemView.findViewById(R.id.txt_space_consumption)

        fun bindHolder(source: Source) {
            sourceName.text = String.format("%s", source.sourceName)
            sourceUsage.text = constructUsageString(source)
            animateProgress(percentBar, source.usedSpacePercent ?: 0)
        }

        private fun constructUsageString(source: Source?): String {
            val usedGb = source?.usedSpaceGB ?: 0.0
            val totalGb = source?.totalSpaceGB ?: 0.0

            return String.format(
                Locale.getDefault(),
                "%.2f / %.2f GB",
                usedGb,
                totalGb
            )
        }

        private fun animateProgress(bar: ProgressBar, maxPercent: Int) =
            ValueAnimator.ofInt(0, maxPercent).apply {
                addUpdateListener { animation: ValueAnimator ->
                    val progress = animation.animatedValue as Int
                    bar.progress = progress
                }
                duration = Constants.Animation.PROGRESS_DURATION.toLong()

                start()
            }
    }

}