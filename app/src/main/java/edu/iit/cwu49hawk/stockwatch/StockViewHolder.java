package edu.iit.cwu49hawk.stockwatch;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class StockViewHolder extends RecyclerView.ViewHolder
{
    public TextView stockSymbol;
    public TextView stockPrice;
    public TextView priceChange;
    public TextView compName;

    public StockViewHolder(View view)
    {
        super(view);
        stockSymbol = (TextView)view.findViewById(R.id.symbol);
        stockPrice = (TextView)view.findViewById(R.id.price);
        priceChange = (TextView)view.findViewById(R.id.priceChange);
        compName = (TextView)view.findViewById(R.id.compName);
    }
}
