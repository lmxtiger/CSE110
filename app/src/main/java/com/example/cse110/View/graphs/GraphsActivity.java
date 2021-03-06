package com.example.cse110.View.graphs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.cse110.Controller.Category;
import com.example.cse110.Controller.MonthlyData;
import com.example.cse110.Model.Database;
import com.example.cse110.Model.PagerAdapter;
import com.example.cse110.R;
import com.example.cse110.View.CategoriesListActivity;
import com.example.cse110.View.MainActivity;
import com.example.cse110.View.SettingsActivity;
import com.example.cse110.View.history.HistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A class representing the graphs for PiggyBank.
 * When user presses: See Graph, this page will appear.
 * @author Fan Ding
 * @version April 28
 *
 */
public class GraphsActivity extends AppCompatActivity {


    /**
     * String List of category names of current month
     */
    private List<String> cateArrayList = new ArrayList<>();


    /**
     * List of category cost of current month
     */
    private List<Double> cateCostArrayList = new ArrayList<>();


    /**
     * string contains all details of past data, Month, year, budgets, costs
     */
    private List<String> allMonths = new ArrayList<>();


    /**
     * change cents to dollar, need to divided by 100
     */
    public static final double CentsToDollar = 100.00;


    /**
     * current_month is an object that have all data we want to display for current Month
     */
    private MonthlyData current_month;


    /**
     * Retrieves our database singleton
     */
    private final Database base = Database.getInstance();


    /**
     * Key for pulling an object of monthlyData in the HistoryDetailedActivity
     *
     * @see #onCreate(Bundle)
     */
    public static final String Graphs_DATA_INTENT = "GraphsActivity monthlyData";
    public static final String MONTHLY_DATA_INTENT = "CategoriesListActivity monthlyData";
    public static final String HISTORY_DATA_INTENT = "HistoryActivity monthlyData";
    private static final String LIST_OF_MONTHS = "List of Months"; //For past months in HistoryActivity.java
    private static final int NAV_BAR_INDEX = 2;
    private MonthlyData thisMonthsData;


    /**
     * The only constructor for instantiating the pie chart page
     * @param savedInstanceState
     * @see AppCompatActivity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        /**
         * navBar handling
         */
        setUpNavBar();

        /**
         * Extract list of months with Year, Month, Budgets, and Expense
         */
        instantiateListOfMonths();

        /**
         * TabLayout implemented here, which allow u to switch between different graphs,
         * like pie Chart and line chart.
         */
        TabLayout tabLayout = findViewById(R.id.tabBar);
        TabItem pieChartTab = findViewById(R.id.pieChartTab);
        TabItem columnChartTab = findViewById(R.id.columnChartTab);
        TabItem lineChartTab = findViewById(R.id.lineChartTab);

        /**
         * set click listener
         */
        PagerAdapterSetUp(tabLayout);

        /**
         * Retrieve passed in MonthlyData object and extract date/categories
         */
        Intent intent = getIntent();
        current_month = intent.getParcelableExtra(Graphs_DATA_INTENT);

        /**
         * store the current_month category and expense into ArrayLists,
         * which will be past to different graphs Fragment and to be used.
         */
        ArrayList<Category> categoryArrayList = current_month.getCategoriesAsArray();
        for (int i = 0; i < categoryArrayList.size(); i++) {
            Category c = categoryArrayList.get(i);
            cateArrayList.add(c.getName());
            cateCostArrayList.add(c.getTotalExpenses() / CentsToDollar);
        }
    }



    public List<String> getCateArrayList() {return cateArrayList;}

    public List<Double> getCateCostArrayList() {return cateCostArrayList;}

    public List<String> getAllMonths(){return allMonths;}


    /**
     * lead user to other fragment when clicking different tab on tabLayout,
     * like pieChart leads user to pieChart fragment.
     * @param tabLayout
     */
    private void PagerAdapterSetUp(TabLayout tabLayout) {
        final ViewPager viewPager = findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }


    /**
     * Handle incoming intents and extract pastMonthSummary's output
     */
    private void instantiateListOfMonths() {
        //Get data from incoming Intent
        Bundle bundle = getIntent().getExtras();

        //Null error handling
        assert bundle != null;

        //Extract months summary
        allMonths = bundle.getStringArrayList(LIST_OF_MONTHS);
    }


    /**
     * The user shall enter any page through clicking the icon in this nav bar
     */
    private void setUpNavBar() {
        // Create the bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // set the label to be visible
        navView.setLabelVisibilityMode(1);
        Menu menu = navView.getMenu();
        // Check the icon
        MenuItem menuItem = menu.getItem(NAV_BAR_INDEX);
        menuItem.setChecked(true);
        navView.setOnNavigationItemSelectedListener(navListener);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the lists page
     */
    private void homePageHandler() {
        //create a new intent to start home page activity
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        setResult(RESULT_OK, intent);
        //put extra monthly data into new intent
        intent.putExtra(MONTHLY_DATA_INTENT, current_month);
        startActivityForResult(intent, 1);
        //avoid shifting
        overridePendingTransition(0, 0);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the history page
     */
    private void historyPageHandler() {
        ValueEventListener Listener = new ValueEventListener() {
            //The onDataChange() method is called every time data is changed at the specified database reference, including changes to children.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent i = new Intent(getBaseContext(), HistoryActivity.class);
                //set up the date for monthly data
                Calendar today = Calendar.getInstance();
                int month = today.get(Calendar.MONTH);
                int year = today.get(Calendar.YEAR);
                //Retrieve the monthly data from the database
                thisMonthsData = base.RetrieveDataCurrent(dataSnapshot, thisMonthsData, year, month);
                //put extra data into new intent
                i.putExtra(HISTORY_DATA_INTENT, thisMonthsData);
                //Add the past month's history (includes current)e
                i.putExtra(LIST_OF_MONTHS, base.getPastMonthSummary(dataSnapshot));
                startActivityForResult(i, 1);
                //avoid shifting
                overridePendingTransition(0, 0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        };
        base.getMyRef().addListenerForSingleValueEvent(Listener);
    }

    /**
     * Helper method to contain the logic for navigation bar to navigate to the graph page
     */
    private void listsPageHandler() {
        /* Read from the database
        / Read data once: addListenerForSingleValueEvent() method triggers once and then does not trigger again.
        / This is useful for data that only needs to be loaded once and isn't expected to change frequently or require active listening.
        */
        ValueEventListener Listener = new ValueEventListener() {
            //The onDataChange() method is called every time data is changed at the specified database reference, including changes to children.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(getBaseContext(), CategoriesListActivity.class);
                //set up the date for monthly data
                Calendar today = Calendar.getInstance();
                int month = today.get(Calendar.MONTH);
                int year = today.get(Calendar.YEAR);
                base.insertMonthlyData(year, month);
                //Retrieve the monthly data from the database
                thisMonthsData = base.RetrieveDataCurrent(dataSnapshot, thisMonthsData, year, month);
                //put extra data into new intent
                intent.putExtra(CategoriesListActivity.MONTHLY_DATA_INTENT, thisMonthsData);
                startActivityForResult(intent, 1);
                //avoid shifting
                overridePendingTransition(0, 0);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        };
        base.getMyRef().addListenerForSingleValueEvent(Listener);
    }

    // BOTTOM NAVIGATION
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            homePageHandler();
                            return true;
                        case R.id.navigation_lists:
                            listsPageHandler();
                            return true;
                        case R.id.navigation_history:
                            historyPageHandler();
                            return true;
                        case R.id.navigation_graphs:
                            return true;
                        case R.id.navigation_settings:
                            //create new intent for settings activity
                            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            return true;
                    }
                    return false;
                }
            };
}