package risq.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.open_offer_item.view.*

class OpenOfferAdapter() : RecyclerView.Adapter<OpenOfferAdapter.OpenOfferViewHolder>() {

    var offers: List<OpenOffer> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenOfferViewHolder {
        return OpenOfferViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.open_offer_item, parent, false)
        )
    }

    override fun getItemCount() = offers.size

    override fun onBindViewHolder(holder: OpenOfferViewHolder, position: Int) {
        val offer = offers.get(position)

        holder.view.textViewDirection.text = offer.direction
        holder.view.textViewPrice.text = offer.formattedPrice
    }

    class OpenOfferViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}