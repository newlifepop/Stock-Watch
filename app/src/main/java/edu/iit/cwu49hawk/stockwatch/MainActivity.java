package edu.iit.cwu49hawk.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener
{
    private RecyclerView recycler;
    private StockAdapter sAdapter;
    private List<Stock> stockList = new ArrayList<>();
    private SwipeRefreshLayout swiper;
    private final String REQUEST_ADD = "ADD";
    private final String REQUEST_RELOAD = "RELOAD";
    private StockDatabaseHandler stockDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stockDatabase = new StockDatabaseHandler(this);
        ArrayList<Stock> stocks = stockDatabase.loadStock();
        stockList.addAll(stocks);
        Collections.sort(stockList);

        if(!hasNetwork()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("No Network Connection");
            builder.setMessage("All Stock Data Have Expired");
            builder.setIcon(R.drawable.ic_warning_black_24px);
            builder.create().show();
        }

        recycler = (RecyclerView)findViewById(R.id.recycler);
        sAdapter = new StockAdapter(stockList, this);
        swiper = (SwipeRefreshLayout)findViewById(R.id.swiper);

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            public void onRefresh()
            {
                if(!hasNetwork())
                {
                    swiper.setRefreshing(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("No Network Connection");
                    builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
                    builder.create().show();
                }
                else
                {
                    swiper.setRefreshing(true);
                    for(int i = 0; i < stockList.size(); ++i)
                    {
                        new StockLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQUEST_RELOAD,
                                stockList.get(i).getStockSymbol(), stockList.get(i).getCompName());
                    }
                    swiper.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "Stocks Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recycler.setAdapter(sAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.addStock:
                if(!hasNetwork())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("No Network Connection");
                    builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
                    builder.create().show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Stock Selection");
                    builder.setMessage("Enter a Stock Symbol:");

                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    input.setGravity(Gravity.CENTER_HORIZONTAL);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                            Editable stockSymbol = input.getText();

                            new StockLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQUEST_ADD, stockSymbol.toString());
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
                break;
        }

        return false;
    }

    private class StockLoader extends AsyncTask<String, String, List<Stock>>
    {
        private String compInfoURL = "http://stocksearchapi.com/api/";
        private String apiKey = "882ca7d8b0dd63ed1ab7fdafe504727bc99b20d2";
        private String stockInfoURL = "http://finance.google.com/finance/info?client=ig";

        @Override
        protected List<Stock> doInBackground(String... params)
        {
            final String REQUEST = params[0];
            final String stockSymbol = params[1];
            final String compName = params.length > 2 ? params[2] : null;

            Uri.Builder buildURL = Uri.parse(compInfoURL).buildUpon();
            buildURL.appendQueryParameter("api_key", apiKey);
            buildURL.appendQueryParameter("search_text", stockSymbol);
            String urlToUse = buildURL.build().toString();

            StringBuilder sb = new StringBuilder();
            try
            {
                URL url = new URL(urlToUse);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                Stock newStock = addStock(sb.toString(), compName);
                publishProgress(REQUEST + ":" + (newStock == null ? "" : newStock.toString()));
            }
            catch(IOException e)
            {
                System.out.println("----------");
                System.out.println("URL TO USE: " + urlToUse);
                System.out.println("----------");

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("No Stock Found");
                        builder.setIcon(R.drawable.ic_warning_black_24px);
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                });
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return stockList;
        }

        protected void onProgressUpdate(String... values)
        {
            String REQUEST = values[0].substring(0, values[0].indexOf(":"));
            String stockInfo = values[0].substring(values[0].indexOf(":") + 1, values[0].length());
            if(stockInfo.length() != 0)
            {
                StringTokenizer strTok = new StringTokenizer(stockInfo, "|");
                String stockSymbol = strTok.nextToken();
                String compName = strTok.nextToken();
                double stockPrice = Double.parseDouble(strTok.nextToken());
                double priceChange = Double.parseDouble(strTok.nextToken());
                double priceChangePerc = Double.parseDouble(strTok.nextToken());

                final Stock newStock = new Stock(stockSymbol, compName, stockPrice, priceChange, priceChangePerc);

                if(REQUEST.equals(REQUEST_ADD))
                {
                    for(int i = 0; i < stockList.size(); ++i)
                    {
                        if(newStock.equals(stockList.get(i)))
                        {
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setIcon(R.drawable.ic_warning_black_24px);
                                    builder.setTitle("Duplicate Stock");
                                    builder.setMessage("Stock " + newStock.getStockSymbol() + " is already displayed");

                                    builder.create().show();
                                }
                            });
                            return;
                        }
                    }
                    stockList.add(newStock);
                    Collections.sort(stockList);
                    stockDatabase.addStock(newStock);
                }
                else if(REQUEST.equals(REQUEST_RELOAD))
                {
                    for(int i = 0; i < stockList.size(); ++i)
                    {
                        if(newStock.equals(stockList.get(i)))
                        {
                            stockList.set(i, newStock);
                            stockDatabase.updateStock(newStock);
                            return;
                        }
                    }
                }
                else {
                    System.out.println("----------REQUEST CODE WRONG----------");
                }
            }
        }

        protected void onPostExecute(List<Stock> updatedStockList)
        {
            sAdapter.notifyDataSetChanged();
        }

        private Stock addStock(String compInfo, String compName)
        {
            try
            {
                JSONArray jsonArray = new JSONArray(compInfo);
                if(jsonArray.length() == 1)
                {
                    JSONObject jsonObject = (JSONObject)jsonArray.get(0);
                    String stockSymbol = jsonObject.getString("company_symbol");
                    String companyName = jsonObject.getString("company_name");

                    return getStockInfo(companyName, stockSymbol);
                }
                else
                {
                    if(compName != null)
                    {
                        JSONObject jsonObject = null;
                        for(int i = 0; i < jsonArray.length(); ++i)
                        {
                            jsonObject = (JSONObject)jsonArray.get(0);
                            if(jsonObject.getString("company_name").equals(compName))
                                break;
                        }
                        String stockSymbol = jsonObject.getString("company_symbol");
                        return getStockInfo(compName, stockSymbol);
                    }
                    else
                    {
                        final String[] stockChoice = new String[jsonArray.length()];
                        final JSONObject[] jsonObjects = new JSONObject[jsonArray.length()];
                        for(int i = 0; i < jsonArray.length(); ++i)
                        {
                            jsonObjects[i] = (JSONObject)jsonArray.get(i);
                            stockChoice[i] = jsonObjects[i].getString("company_symbol") + " - "
                                    + jsonObjects[i].getString("company_name");
                        }

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Make a Selection");
                                builder.setItems(stockChoice, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialogInterface, int which)
                                    {
                                        dialogInterface.dismiss();
                                        JSONObject selectedStock = jsonObjects[which];
                                        try
                                        {
                                            String compName = selectedStock.getString("company_name");
                                            String stockSymbol = selectedStock.getString("company_symbol");

                                            new StockLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQUEST_ADD, stockSymbol, compName);
                                        }
                                        catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                builder.create().show();
                            }
                        });
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private Stock getStockInfo(String compName, String stockSymbol)
        {
            Uri.Builder buildURL = Uri.parse(stockInfoURL).buildUpon();
            buildURL.appendQueryParameter("q", stockSymbol);
            String urlToUse = buildURL.build().toString();

            StringBuilder sb = new StringBuilder();
            try
            {
                URL url = new URL(urlToUse);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line = reader.readLine()) != null)
                    sb.append(line).append('\n');

                sb.delete(0, 1);

                JSONObject jsonObject = new JSONObject(sb.toString());
                double stockPrice = Double.parseDouble(jsonObject.getString("l"));
                String priceChangeString = jsonObject.getString("c");
                int sign = priceChangeString.contains("+")?1:-1;
                double priceChange = Double.parseDouble(priceChangeString.substring(1, priceChangeString.length()));
                double priceChangePerc = Double.parseDouble(jsonObject.getString("cp"));

                return new Stock(stockSymbol, compName, stockPrice, sign * priceChange, priceChangePerc);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public void onClick(View v)
    {
        if(hasNetwork())
        {
            String url = "http://www.marketwatch.com/investing/stock/";
            final int pos = recycler.getChildLayoutPosition(v);
            url += stockList.get(pos).getStockSymbol();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Viewed Without A Network Connection");
            builder.setIcon(R.drawable.ic_warning_black_24px);
            builder.create().show();
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        final int pos = recycler.getChildLayoutPosition(v);
        final Stock current = stockList.get(pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_delete_black_24px);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + current.getStockSymbol() + "?");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                stockDatabase.deleteStock(current.getStockSymbol());
                stockList.remove(pos);
                sAdapter.notifyDataSetChanged();
            }
        });

        builder.create().show();

        return false;
    }

    public boolean hasNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnectedOrConnecting())
            return false;

        return true;
    }
}