package edu.iit.cwu49hawk.stockwatch;

public class Stock implements Comparable<Stock>
{
    private String stockSymbol;
    private String compName;
    private double stockPrice;
    private double priceChange;
    private double changePerc;

    public Stock(String stockSymbol, String compName,
                 double stockPrice, double priceChange, double changePerc)
    {
        this.stockSymbol = stockSymbol;
        this.compName = compName;
        this.stockPrice = stockPrice;
        this.priceChange = priceChange;
        this.changePerc = changePerc;
    }

    public String getStockSymbol()
    {
        return this.stockSymbol;
    }

    public String getCompName()
    {
        return this.compName;
    }

    public double getStockPrice()
    {
        return this.stockPrice;
    }

    public double getPriceChange()
    {
        return this.priceChange;
    }

    public double getChangePerc()
    {
        return this.changePerc;
    }

    public boolean equals(Stock stock)
    {
        if(stock.getStockSymbol().equals(stockSymbol) && stock.getCompName().equals(this.compName))
            return true;

        return false;
    }

    public String toString()
    {
        return this.stockSymbol + "|" + this.compName + "|" +
                this.stockPrice + "|" + this.priceChange + "|" +this.changePerc;
    }

    @Override
    public int compareTo(Stock stock)
    {
        if(this.stockSymbol.compareToIgnoreCase(stock.getStockSymbol()) > 0)
            return 1;
        else if(this.stockSymbol.compareToIgnoreCase(stock.getStockSymbol()) < 0)
            return -1;

        return 0;
    }
}