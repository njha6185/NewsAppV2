package com.example.nitishkumar.newsappv2;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsData>>,
        View.OnClickListener, android.support.v7.widget.SearchView.OnQueryTextListener,
        NavigationView.OnNavigationItemSelectedListener{

    private static final String GUARDIAN_API_REQUEST_URL = "https://content.guardianapis.com" ;
    private String searchQuery ;
    private String newsSection;
    private android.support.v7.widget.SearchView searchView;
    private static final int NEWS_LOADER_ID = 1;
    private ArrayList<NewsData> newsdataArray;
    private NewsDataAdapter mNewsAdapter;
    LoaderManager loaderManager;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private ActionBarDrawerToggle toggle;
    @BindView(R.id.empty_view) TextView mEmptyStateTextView;
    @BindView(R.id.recyclerViewList) RecyclerView recyclerView;
    @BindView(R.id.loading_indicator) View loadingIndicator;
    @BindView(R.id.fromDateText) TextView fromDateTextView;
    @BindView(R.id.toDateText) TextView toDateTextView;
    @BindView(R.id.refreshImage) ImageView refreshImageView;
    @BindView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        newsSection = getString(R.string.all);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.all);

        fromDateTextView.setText(currentSystemdate());
        toDateTextView.setText(currentSystemdate());

        fromDateTextView.setOnClickListener(this);
        toDateTextView.setOnClickListener(this);
        refreshImageView.setOnClickListener(this);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        newsdataArray = new ArrayList<>();
        mNewsAdapter = new NewsDataAdapter(this, newsdataArray);
        recyclerView.setAdapter(mNewsAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                NewsData currentNewsData = mNewsAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNewsData.getNewsWebURL());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        else {
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.connectivity_service_problem);
        }

        if (networkInfo != null && networkInfo.isConnected())
        {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        }
        else {
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<NewsData>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPreferences.getString(getString(R.string.setting_order_by_key), getString(R.string.setting_order_by_default));
        String newsAreaBy = sharedPreferences.getString(getString(R.string.setting_news_area_key), getString(R.string.setting_news_area_default));
        String noOfNewsPerPage = sharedPreferences.getString(getString(R.string.setting_no_of_news_per_page_key), getString(R.string.setting_no_of_news_per_page_default));
        String fromDate = fromDateTextView.getText().toString();
        String toDate = toDateTextView.getText().toString();
        newsSection = newsSection.toLowerCase();

        Uri baseUri = Uri.parse(GUARDIAN_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        if (newsAreaBy.equalsIgnoreCase(getString(R.string.setting_news_area_by_india_value)))
        {
            uriBuilder.appendPath(getString(R.string.setting_news_area_by_world_value));
        }
        uriBuilder.appendPath(newsAreaBy);
        if (searchQuery != null)
        {
            uriBuilder.appendQueryParameter("q", searchQuery);
        }
        uriBuilder.appendQueryParameter("format","json");
        if (!newsSection.equalsIgnoreCase(getString(R.string.all)))
        {
            uriBuilder.appendQueryParameter("section",newsSection);
        }
        uriBuilder.appendQueryParameter("from-date",fromDate);
        uriBuilder.appendQueryParameter("to-date",toDate);
        uriBuilder.appendQueryParameter("page-size",noOfNewsPerPage);
        uriBuilder.appendQueryParameter("page","1");
        uriBuilder.appendQueryParameter("show-fields","headline,thumbnail,short-url");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", getString(R.string.API_KEY));
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsData>> loader, List<NewsData> data) {
        loadingIndicator.setVisibility(View.GONE);
        newsdataArray.clear();

        if (data != null && !data.isEmpty())
        {
            newsdataArray.addAll(data);
            mNewsAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setVisibility(View.GONE);
        }
        else
        {
            connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            else {
                loadingIndicator.setVisibility(View.GONE);
                mEmptyStateTextView.setText(R.string.connectivity_service_problem);
            }
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (networkInfo != null && networkInfo.isConnected())
            {
                mEmptyStateTextView.setText(R.string.no_news);
            }
            else
            {
                mEmptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsData>> loader) {
        newsdataArray.clear();
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        newsdataArray.clear();
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (android.support.v7.widget.SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting)
        {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        if (toggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String currentSystemdate()
    {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    @Override
    public void onClick(final View v) {

        if (v == fromDateTextView || v == toDateTextView)
        {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String formattedDate = "";
                    String dateString = year + "-" + month + "-" + dayOfMonth;
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
                    try {
                        Date d = df.parse(dateString);
                        df = new SimpleDateFormat("yyyy-MM-dd");
                        formattedDate = df.format(d);
                    } catch (ParseException e) {
                        Log.e("MainActivity", "Date parsing Problem "+e);
                    }

                    if (v == fromDateTextView)
                    {
                        fromDateTextView.setText(formattedDate);
                        Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();
                    }
                    if (v == toDateTextView)
                    {
                        toDateTextView.setText(formattedDate);
                        Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();
                    }
                }
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if (v == refreshImageView)
        {
            recyclerView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
            Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Search For " + query, Toast.LENGTH_SHORT).show();
        searchQuery = query;
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.all)
        {
            getSupportActionBar().setTitle(R.string.all);
            newsSection = getString(R.string.all);
            navigationView.setCheckedItem(R.id.all);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.film)
        {
            getSupportActionBar().setTitle(R.string.film);
            newsSection = getString(R.string.film);
            navigationView.setCheckedItem(R.id.film);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.world)
        {
            getSupportActionBar().setTitle(R.string.world);
            navigationView.setCheckedItem(R.id.world);
            newsSection = getString(R.string.world);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.sport)
        {
            getSupportActionBar().setTitle(R.string.sport);
            newsSection = getString(R.string.sport);
            navigationView.setCheckedItem(R.id.sport);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.football)
        {
            getSupportActionBar().setTitle(R.string.football);
            newsSection = getString(R.string.football);
            navigationView.setCheckedItem(R.id.football);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.culture)
        {
            getSupportActionBar().setTitle(R.string.culture);
            newsSection = getString(R.string.culture);
            navigationView.setCheckedItem(R.id.culture);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.cricket)
        {
            getSupportActionBar().setTitle(R.string.cricket);
            newsSection = getString(R.string.cricket);
            navigationView.setCheckedItem(R.id.cricket);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.business)
        {
            getSupportActionBar().setTitle(R.string.business);
            newsSection = getString(R.string.business);
            navigationView.setCheckedItem(R.id.business);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.fashion)
        {
            getSupportActionBar().setTitle(R.string.fashion);
            newsSection = getString(R.string.fashion);
            navigationView.setCheckedItem(R.id.fashion);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.technology)
        {
            getSupportActionBar().setTitle(R.string.technology);
            newsSection = getString(R.string.technology);
            navigationView.setCheckedItem(R.id.technology);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.travel)
        {
            getSupportActionBar().setTitle(R.string.travel);
            newsSection = getString(R.string.travel);
            navigationView.setCheckedItem(R.id.travel);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.finance)
        {
            getSupportActionBar().setTitle(R.string.finance);
            newsSection = getString(R.string.finance);
            navigationView.setCheckedItem(R.id.finance);
            drawerLayout.closeDrawers();
        }
        else if (item.getItemId() == R.id.science)
        {
            getSupportActionBar().setTitle(R.string.science);
            newsSection = getString(R.string.science);
            navigationView.setCheckedItem(R.id.science);
            drawerLayout.closeDrawers();
        }
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        return true;
    }
}