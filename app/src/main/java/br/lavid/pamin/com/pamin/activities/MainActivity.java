package br.lavid.pamin.com.pamin.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.pkmmte.view.CircularImageView;

import java.util.LinkedList;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.adapter.DrawerListAdapter;
import br.lavid.pamin.com.pamin.fragments.MainListFragment;
import br.lavid.pamin.com.pamin.fragments.MainMapFragment;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.sync.SyncPamimData;
import br.lavid.pamin.com.pamin.utils.InternetFeatures;
import br.lavid.pamin.com.pamin.utils.TabletFeatures;

/**
 * First Activity to run in the app
 * Last update: 07/19/2015
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_REPORT = 404;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS= 102;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET= 103;
    private static final int MY_PERMISSIONS_REQUEST_TO_READ_EXTERNAL_STORAGE = 104;

    public static LinkedList<CulturalRegister> actCulturalRegisters;

    //FACEBOOK
    public static CallbackManager callbackManager;
    private static MainListFragment mainListFragment;
    private static MainMapFragment mainMapFragment;
    //USER INFO
    private GoogleApiClient mGoogleApiClient;
    //PAMIN INFO
    private SyncPamimData sync;

    //ACTIVITY
    private Toolbar toolbar;
    private FloatingActionsMenu fab;
    private ViewPager pager;
    private PagerSlidingTabStrip tabs;
    private MainPagerAdapter mainPagerAdapter;

    //NAVIGATION DRAWER
    private LinearLayout userProfile;
    private CircularImageView userPicture;
    private TextView usernameTV, emailTV;

    private ListView navigationListView;
    private DrawerListAdapter mAdapter;
    private DrawerLayout Drawer;
    private ActionBarDrawerToggle mDrawerToggle;

    //AUX
    private int auxPermission = 0;


    /**
     * Get a list with ALL the cultural registers (will be change later with the new API)
     *
     * @return
     */
    public static LinkedList<CulturalRegister> getActCulturalRegisters() {
        return actCulturalRegisters;
    }

    /**
     * Call this method to update the list and the map with the cultural registers list
     */
    public static void updateLists() {
        mainListFragment.updateList();
        mainMapFragment.updateList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (Build.VERSION.SDK_INT >= 23) {
            int count = 0;
            verifyPermission();
            while (true) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    break;
                }
                if(auxPermission == -1) {
                    break;
                }
            }
        }

        if(auxPermission == -1) {
            return;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());


        initUserData();
        initBaseActivity();
        initNavDrawer();

        initFloatingButton();
        initTabletOrSmartphoneView();

        if (!InternetFeatures.hasInternet(this)) {
            Toast.makeText(this, " Acesse a internet para atualizar os cards ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.collapse();

        if (User.getInstance(this).hasUser()) {
            String name = User.getInstance(this).getName();
            String[] names = name.split("\\s+");
            try {
                usernameTV.setText(names[0] + " " + names[1]);
            } catch (ArrayIndexOutOfBoundsException overTheLimit) {
                usernameTV.setText(name);
            }
            emailTV.setText(User.getInstance(this).getEmail());
            if (User.getInstance(MainActivity.this).getUserPicture() != null)
                userPicture.setImageBitmap(User.getInstance(MainActivity.this).getUserPicture());
            if (User.getInstance(MainActivity.this).getBackground() != null)
                userProfile.setBackground(new BitmapDrawable(getResources(), User.getInstance(MainActivity.this).getBackground()));
        }

        initPaminData();
    }

    /**
     * Get user location info
     */
    private void initUserData() {
        if (User.getInstance(this).getUserLocation() == null)
            buildGoogleApiClient();
    }

    /**
     * Go to position on map
     * TODO: Go to the position on list
     *
     * @param latLng the position to go
     */
    public void goToPosition(LatLng latLng) {
        if (!new TabletFeatures().isTablet(this))
            pager.setCurrentItem(2);
        mainMapFragment.goToPosition(latLng);
    }

    /**
     * Change the list itens with the category defined
     * TODO: Change the position on Map
     *
     * @param name
     */
    public void setCategory(String name) {
        mainListFragment.setCategory(name);
        mainMapFragment.setCategory(name);
        Log.e("setCategory is here", "So testando mesmo");
        Drawer.closeDrawers();
    }

    private void initFloatingButton() {

        fab = (FloatingActionsMenu) findViewById(R.id.main_floatingBtn);
        fab.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {

            RelativeLayout dimView = (RelativeLayout) findViewById(R.id.main_dim);

            @Override
            public void onMenuExpanded() {
                if (!User.getInstance(MainActivity.this).hasUser()) {
                    fab.collapseImmediately();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    builder.setMessage(getString(R.string.main_dialog_loginAsker))
                            .setPositiveButton(getString(R.string.Yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.No), dialogClickListener).show();

                    return;
                }
                dimView.setVisibility(View.VISIBLE);
                dimView.setBackgroundColor(getResources().getColor(R.color.white_semi_transparent));

                dimView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fab.collapse();
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                dimView.setVisibility(View.GONE);
            }
        });

        FloatingActionButton celebBtn = (FloatingActionButton) findViewById(R.id.add_celebBtn);
        FloatingActionButton expBtn = (FloatingActionButton) findViewById(R.id.add_exp);
        FloatingActionButton knowBtn = (FloatingActionButton) findViewById(R.id.add_knowBtn);
        FloatingActionButton objBtn = (FloatingActionButton) findViewById(R.id.add_objBtn);
        FloatingActionButton peoBtn = (FloatingActionButton) findViewById(R.id.add_peoBtn);
        FloatingActionButton placesBtn = (FloatingActionButton) findViewById(R.id.add_placesBtn);

        View.OnClickListener floatingClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.add_celebBtn: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Celebrações");
                        startActivity(register);
                        break;
                    }
                    case R.id.add_exp: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Formas de Expressão");
                        startActivity(register);
                        break;
                    }
                    case R.id.add_knowBtn: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Saberes");
                        startActivity(register);
                        break;
                    }
                    case R.id.add_objBtn: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Objetos");
                        startActivity(register);
                        break;
                    }
                    case R.id.add_peoBtn: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Pessoas");
                        startActivity(register);
                        break;
                    }
                    case R.id.add_placesBtn: {
                        Intent register = new Intent(getApplication(), NewRegisterActivity.class);
                        register.putExtra("category", "Lugares");
                        startActivity(register);
                        break;
                    }
                }
            }
        };

        celebBtn.setOnClickListener(floatingClick);
        placesBtn.setOnClickListener(floatingClick);
        expBtn.setOnClickListener(floatingClick);
        knowBtn.setOnClickListener(floatingClick);
        objBtn.setOnClickListener(floatingClick);
        peoBtn.setOnClickListener(floatingClick);

    }

    /**
     * Check the device type (smartphone or tablet) and set the default style for then
     */
    private void initTabletOrSmartphoneView() {

        if (!TabletFeatures.isTablet(getApplicationContext())) { //IS SMARTPHONE
            Log.v("Main", "Smartphone");
            initMaterialTab();
        } else { //TABLET
            Log.v("Main", "Tablet");
            mainListFragment = (MainListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.main_listFrag);
            mainMapFragment = (MainMapFragment) getSupportFragmentManager().findFragmentById(
                    R.id.main_mapFrag);
        }
    }

    /**
     * Build API Client to get User Location
     */
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (User.getInstance(MainActivity.this).getUserLocation() == null)
                            if(Build.VERSION.SDK_INT>=23) {
                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                            }
                            User.getInstance(MainActivity.this).setUserLocation(LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient));
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Init Pamin Sync, that will load the offline list on the device, and update with the online list
     * in the background
     */
    private void initPaminData() {
        sync = new SyncPamimData(this);
        actCulturalRegisters = sync.getAll(new SyncPamimData.UpdateList() {
            @Override
            public void updateList() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actCulturalRegisters = sync.getOffList();
                        updateLists();
                    }
                });
            }
        });
    }

    /**
     * ONLY ON SMARTPHONE
     * This will load the tabs on smartphone
     */
    private void initMaterialTab() {

        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(mainPagerAdapter);
        tabs.setViewPager(pager);
    }

    /**
     * Load the toolbar
     */
    private void initBaseActivity() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
    }

    /**
     *  Load the navigation drawer
     */
    private void initNavDrawer() {

        //LISTVIEW
        navigationListView = (ListView) findViewById(R.id.navigation_listview);
        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mainListFragment != null)
                    setCategory(getResources().getStringArray(R.array.mainmenu_category_array)[position - 1]);
            }
        });
        mAdapter = new DrawerListAdapter(this);

        //HEADER
        userProfile = (LinearLayout) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.maindrawer_userprofile, navigationListView, false);
        navigationListView.addHeaderView(userProfile);
        userPicture = (CircularImageView) userProfile.findViewById(R.id.userProfile_imageView);
        usernameTV = (TextView) userProfile.findViewById(R.id.userProfile_nick);
        emailTV = (TextView) userProfile.findViewById(R.id.userprofile_subnick);

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getInstance(MainActivity.this).hasUser())
                    startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (User.getInstance(MainActivity.this).hasUser())
                    startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        navigationListView.setAdapter(mAdapter);

        //DRAWER
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

        };
        Drawer.addDrawerListener(mDrawerToggle); //Trocou de set para add
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        if (id == R.id.action_reportProblem) {
            startActivityForResult(new Intent(this, ReportProblemActivity.class), REQUEST_REPORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_REPORT && resultCode == RESULT_OK) {
            Toast.makeText(this, data.getStringExtra(ReportProblemActivity.RESULT_REPORT), Toast.LENGTH_LONG).show();
            return;
        }

        try { //API DO FACEBOOK DÁ CRASH SEM MOTIVOS, NÃO TIRAR
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } catch (Exception error) {
        }
    }

    /**
     * JUST ON SMARTPHONE
     * This will load the tabs
     */
    public class MainPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getResources().getString(R.string.mainActivity_listTab),
                getResources().getString(R.string.mainActivity_mapTab)};


        public MainPagerAdapter(FragmentManager fm) {
            super(fm);

            mainMapFragment = new MainMapFragment();
            mainListFragment = new MainListFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return mainListFragment;
                case 1:
                    return mainMapFragment;
                default:
                    return mainMapFragment;
            }
        }
    }

    private void verifyPermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION );
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.GET_ACCOUNTS)) {

            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.INTERNET)) {

            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {


            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_TO_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    auxPermission = -1;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    auxPermission = -1;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    auxPermission = -1;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    auxPermission = -1;
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_TO_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    auxPermission = -1;
                }
                return;
            }

        }
    }
}
