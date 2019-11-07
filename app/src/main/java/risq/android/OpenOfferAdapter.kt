package risq.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.open_offer_item.view.*

class OpenOfferAdapter() : RecyclerView.Adapter<OpenOfferAdapter.OpenOfferViewHolder>() {

    var buys: List<OpenOffer> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var sells: List<OpenOffer> = ArrayList()
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

    override fun getItemCount() = Integer.max(buys.size, sells.size)

    override fun onBindViewHolder(holder: OpenOfferViewHolder, position: Int) {
        if (sells.size > position) {
            val offer = sells.get(position)

            holder.view.sellDirection.text = offer.direction
            holder.view.sellPrice.text = offer.formattedAmount + " @\n" + offer.formattedPrice
        } else {
            holder.view.sellDirection.text = ""
            holder.view.sellPrice.text = ""
        }
        if (buys.size > position) {
            val offer = buys.get(position)

            holder.view.buyDirection.text = offer.direction
            holder.view.buyPrice.text = offer.formattedAmount + " @\n" + offer.formattedPrice
        } else {
            holder.view.buyDirection.text = ""
            holder.view.buyPrice.text = ""
        }
    }

    class OpenOfferViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}