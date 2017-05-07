package edu.iit.cwu49hawk.stockwatch;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder>
{
    private List<Stock> stockList;
    private MainActivity mainActivity;

    public StockAdapter(List<Stock> stockList, MainActivity mainActivity)
    {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    public StockViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_entry, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    public void onBindViewHolder(StockViewHolder holder, int position)
    {
        Stock stock = stockList.get(position);
        if(stock.getPriceChange() >= 0)
        {
            holder.stockSymbol.setTextColor(Color.GREEN);
            holder.stockPrice.setTextColor(Color.GREEN);
            holder.priceChange.setTextColor(Color.GREEN);
            holder.compName.setTextColor(Color.GREEN);
            holder.stockSymbol.setText(stock.getStockSymbol());
            holder.stockPrice.setText(String.format("%.2f", stock.getStockPrice()));
            holder.priceChange.setText(String.format("▴ %.2f(%.2f%%)",
                    stock.getPriceChange(), stock.getChangePerc()));
            holder.compName.setText(stock.getCompName());
        }
        else
        {
            holder.stockSymbol.setTextColor(Color.RED);
            holder.stockPrice.setTextColor(Color.RED);
            holder.priceChange.setTextColor(Color.RED);
            holder.compName.setTextColor(Color.RED);

            holder.stockSymbol.setText(stock.getStockSymbol());
            holder.stockPrice.setText(String.format("%.2f", stock.getStockPrice()));
            holder.priceChange.setText(String.format("▾ %.2f(%.2f%%)",
                    stock.getPriceChange(), stock.getChangePerc()));
            holder.compName.setText(stock.getCompName());
        }
    }

    public int getItemCount() {
        return this.stockList.size();
    }
}