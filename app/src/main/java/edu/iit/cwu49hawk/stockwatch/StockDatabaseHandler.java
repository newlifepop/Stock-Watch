package edu.iit.cwu49hawk.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by wsy37 on 3/18/2017.
 */

public class StockDatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockDB";
    private static final String TABLE_NAME = "StockTable";
    private static final String STOCKSYMBOL = "stockSymbol";
    private static final String COMPANYNAME = "compName";
    private static final String STOCKPRICE = "stockPrice";
    private static final String PRICECHANGE = "priceChange";
    private static final String PRICECHANGEPERCENT = "priceChangePerc";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    STOCKSYMBOL + " TEXT not null unique," +
                    COMPANYNAME + " TEXT not null, " +
                    STOCKPRICE + " DOUBLE not null, " +
                    PRICECHANGE + " DOUBLE not null, " +
                    PRICECHANGEPERCENT + " DOUBLE not null)";

    private SQLiteDatabase database;

    public StockDatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<Stock> loadStock()
    {
        ArrayList<Stock> stocks = new ArrayList<>();
        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{STOCKSYMBOL, COMPANYNAME, STOCKPRICE, PRICECHANGE, PRICECHANGEPERCENT},
                null,
                null,
                null,
                null,
                null);
        if(cursor != null)
        {
            cursor.moveToFirst();

            for(int i = 0; i < cursor.getCount(); ++i)
            {
                String stockSymbol = cursor.getString(0);
                String compName = cursor.getString(1);
                double stockPrice = cursor.getDouble(2);
                double priceChange = cursor.getDouble(3);
                double priceChangePerc = cursor.getDouble(4);
                stocks.add(new Stock(stockSymbol, compName, stockPrice, priceChange, priceChangePerc));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return stocks;
    }

    public void addStock(Stock stock)
    {
        ContentValues values = new ContentValues();
        values.put(STOCKSYMBOL, stock.getStockSymbol());
        values.put(COMPANYNAME, stock.getCompName());
        values.put(STOCKPRICE, stock.getStockPrice());
        values.put(PRICECHANGE, stock.getPriceChange());
        values.put(PRICECHANGEPERCENT, stock.getChangePerc());

        deleteStock(stock.getStockSymbol());
        database.insert(TABLE_NAME, null, values);
    }

    public void deleteStock(String stockSymbol)
    {
        database.delete(TABLE_NAME, STOCKSYMBOL + " = ?", new String[]{stockSymbol});
    }

    public void updateStock(Stock stock)
    {
        ContentValues values = new ContentValues();
        values.put(STOCKSYMBOL, stock.getStockSymbol());
        values.put(COMPANYNAME, stock.getCompName());
        values.put(STOCKPRICE, stock.getStockPrice());
        values.put(PRICECHANGE, stock.getPriceChange());
        values.put(PRICECHANGEPERCENT, stock.getChangePerc());

        database.update(TABLE_NAME, values, STOCKSYMBOL + " = ?", new String[]{stock.getStockSymbol()});
    }
}