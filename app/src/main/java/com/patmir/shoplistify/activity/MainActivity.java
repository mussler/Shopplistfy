package com.patmir.shoplistify.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.patmir.shoplistify.R;
import com.patmir.shoplistify.adapter.RecyclerViewMainListAdapter;
import com.patmir.shoplistify.model.DataSet;
import com.patmir.shoplistify.model.Product;
import com.patmir.shoplistify.model.ProductList;
import com.patmir.shoplistify.model.Settings;
import com.patmir.shoplistify.model.User;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, GoogleApiClient.OnConnectionFailedListener {

    private Toolbar mToolbar;
    private ArrayList<ProductList> lists_bag =  new ArrayList<ProductList>();
    private RecyclerView recyclerView;
    private RecyclerViewMainListAdapter recyclerViewMainListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DataSet dataSet;
    private static Context mContext;
    private boolean mSearchCheck;
    private DrawerLayout drawerLayout;
    private Settings settings;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount acct = null;
    private boolean drawerInit = false;
    private Handler navHandler = new Handler();

    /* The login button for Google */
    private SignInButton mGoogleLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        //Menu Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initDrawer();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestId().requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /*if (!mGoogleApiClient.isConnected()) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    Log.e("AUTH LOG", "Account wasnt null");
                    User user = new User(acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl());
                    getGoogleOAuthTokenAndLogin(user);
                }
                handleSignInResult(result);
            } else {
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }


        }*/
        DataSet.setInstance();
        settings = DataSet.getSettings();
        lists_bag = DataSet.getData();
        Log.e("Main", "Bag Size: "+lists_bag.size());
        layoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.list_main);
        recyclerView.setLayoutManager(layoutManager);        //Listeners
        findViewById(R.id.new_list_btn).setOnClickListener(this);
        recyclerViewMainListAdapter = new RecyclerViewMainListAdapter(lists_bag, settings);
        recyclerView.setAdapter(recyclerViewMainListAdapter);
        updateUI();




    }

    private void initDrawer(){
        //Navigation
        //Initializing NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.drawer_settings || menuItem.getItemId() == R.id.drawer_account) {

                } else {
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mToolbar.setTitle(getResources().getString(R.string.app_name));
                        recyclerViewMainListAdapter.animateTo(lists_bag);
                        recyclerView.scrollToPosition(0);
                        drawerLayout.closeDrawers();
                        return true;
                    } else{
                        menuItem.setChecked(true);
                }
            }

                //Closing drawer on item click
                drawerLayout.closeDrawers();
                final ArrayList<ProductList> filteredData;
                switch (menuItem.getItemId()) {

                    case R.id.drawer_cat_groceries:
                        filteredData = filter(lists_bag, 0);
                        mToolbar.setTitle(getResources().getStringArray(R.array.list_categories)[0]);
                        recyclerViewMainListAdapter.animateTo(filteredData);
                        recyclerView.scrollToPosition(0);
                        return true;
                    case R.id.drawer_cat_electronics:
                        mToolbar.setTitle(getResources().getStringArray(R.array.list_categories)[1]);
                        filteredData = filter(lists_bag, 1);
                        recyclerViewMainListAdapter.animateTo(filteredData);
                        recyclerView.scrollToPosition(0);
                        return true;
                    case R.id.drawer_cat_other:
                        mToolbar.setTitle(getResources().getStringArray(R.array.list_categories)[2]);
                        filteredData = filter(lists_bag, 2);
                        recyclerViewMainListAdapter.animateTo(filteredData);
                        recyclerView.scrollToPosition(0);
                        return true;
                    case R.id.drawer_settings:
                        final Intent intent;
                        intent = new Intent(mContext, SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, 1);
                        return true;
                    case R.id.drawer_account:
                        Log.e("Drawer", "Clicked account");
                        if(DataSet.getUser() != null){
                            Log.e("Drawer", "Not Null");
                            signOut();
                        } else {
                            Log.e("Drawer", "Null");
                            signIn();
                        }
                        return true;
                    default:
                        return true;

                }
            }
        });


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,mToolbar,R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        setAccountDisplay();
        actionBarDrawerToggle.syncState();

    }
    private void setAccountDisplay(){
                TextView username = (TextView) findViewById(R.id.user_header_username);
                TextView email = (TextView) findViewById(R.id.user_header_email);
                CircleImageView profile_picture = (CircleImageView) findViewById(R.id.user_header_profile_image);
            if(username != null){
                if (DataSet.getUser() != null) {
                    User user = DataSet.getUser();
                    Picasso.with(getBaseContext()).load(user.getPic_url()).into(profile_picture);
                    username.setText(user.getName());
                    email.setText(user.getEmail());

                } else {
                    profile_picture.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_user));
                    username.setText("no user");
                    email.setText("");
                }
            }

    };
    public void updateUI(){
        Log.e("UpdateUI", "Starting");
                TextView empty_info = (TextView) findViewById(R.id.main_list_empty_info);

                if (lists_bag.size() == 0) {
                    empty_info.setVisibility(View.VISIBLE);
                } else {
                    empty_info.setVisibility(View.GONE);
                    recyclerViewMainListAdapter.setData(DataSet.getData());
                    recyclerViewMainListAdapter.setSettings(DataSet.getSettings());
                    recyclerViewMainListAdapter.animateTo(DataSet.getData());

                }

                List<String> c = Arrays.asList(getResources().getStringArray(R.array.list_categories));
                if(c.contains(mToolbar.getTitle())){

                   filterCategory(c.indexOf(mToolbar.getTitle()));
                } else {
                    recyclerViewMainListAdapter.animateTo(lists_bag);
                }
setAccountDisplay();
        recyclerViewMainListAdapter.refresh();

    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.action_bar_list, menu);

                //Select search item
                final MenuItem menuItem = menu.findItem(R.id.menu_search);
                menuItem.setVisible(true);

                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.search));

                ((EditText) searchView.findViewById(R.id.search_src_text))
                        .setHintTextColor(getResources().getColor(R.color.white));
                searchView.setOnQueryTextListener(onQuerySearchView);

                menu.findItem(R.id.menu_delete).setVisible(false);

                mSearchCheck = true;

        return true;
    }
    public static Context getContext() {
        return mContext;
    }
    public void filterCategory(int cat){
        if(cat < 0){
            new Thread(new Runnable() {
                public void run() {
                    mToolbar.setTitle(getResources().getString(R.string.app_name));
                    recyclerViewMainListAdapter = new RecyclerViewMainListAdapter(lists_bag, settings);
                    recyclerView.setAdapter(recyclerViewMainListAdapter);
                }}).run();
            return;
        }
        mToolbar.setTitle(getResources().getStringArray(R.array.list_categories)[cat]);

        for(ProductList p : lists_bag){
            if(p.getCategory() == cat){
            }
        }
        new Thread(new Runnable() {
            public void run() {
                recyclerView.setAdapter(recyclerViewMainListAdapter);
            }}).run();


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_delete
                ) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()){
            case R.id.new_list_btn:
                new Thread(new Runnable() {
                    public void run() {
                        openNewListDialog(v);
                    }}).run();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        //TODO: do it properly
        switch(v.getId()){
            case R.id.new_list_btn:
                break;
        }
        return true;
    }

    public void openNewListDialog(final View v){
        AlertDialog.Builder builder =  new AlertDialog.Builder(v.getContext());
        LayoutInflater factory = LayoutInflater.from(v.getContext());
        final View new_list_dialog_view = factory.inflate(R.layout.add_new_list_dialog, null);
        builder.setView(new_list_dialog_view);
        builder.setTitle(R.string.add_new_list_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
        ;
        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                EditText name = (EditText) new_list_dialog_view.findViewById(R.id.new_list_name);
                AppCompatSpinner cat = (AppCompatSpinner) new_list_dialog_view.findViewById(R.id.list_category_spinner);
                if (name.getText().toString().trim().length() > 0) {
                    ProductList pL = new ProductList(name.getText().toString(), cat.getSelectedItemPosition());
                    lists_bag.add(0, pL );
                    Log.e("New List", "Bag size: "+lists_bag.size());
                    DataSet.setData(lists_bag);
                    Log.e("New List", "Dataset data size: "+DataSet.getData().size());
                    DataSet.saveCache();
                    updateUI();
                    wantToCloseDialog = true;
                } else {
                    name.setError("You must give a name.");
                }
                if (wantToCloseDialog)
                    dialog.dismiss();
            }
        });

    }
    private SearchView.OnQueryTextListener onQuerySearchView = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }
        @Override
        public boolean onQueryTextChange(String s) {
            if (mSearchCheck) {  // Search while typing, min 3 characters
                if (s.length() > 0) {
                    final ArrayList<ProductList> filteredData = filter(lists_bag, s);
                    recyclerViewMainListAdapter.animateTo(filteredData);

                } else {
                    recyclerViewMainListAdapter.animateTo(lists_bag);
                }
                recyclerView.scrollToPosition(0);
            }
            return false;
        }
    };
    private ArrayList<ProductList> filter(ArrayList<ProductList> pLs, String s){
        s = s.toLowerCase();
        final ArrayList<ProductList> filteredPls = new ArrayList<>();
        for (ProductList pl : pLs){
            final String text = pl.getName().toLowerCase();
            if(text.contains(s)){
                filteredPls.add(pl);
            }
        }
        return filteredPls;
    }
    private ArrayList<ProductList> filter(ArrayList<ProductList> pLs, int cat){
        final ArrayList<ProductList> filteredPls = new ArrayList<>();
        for (ProductList pl : pLs){
            final int category = pl.getCategory();
            if(category == cat){
                filteredPls.add(pl);
            }
        }
        return filteredPls;
    }
    private SearchView.OnCloseListener onCloseListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {

            recyclerViewMainListAdapter.animateTo(lists_bag);
            recyclerView.scrollToPosition(0);
            updateUI();
            return false;
        }
    };
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        updateUI();


    }


    private void getGoogleOAuthTokenAndLogin(final User user) {
        Log.e("AUTH LOG", "Getting Token");
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(MainActivity.this, user.getEmail(), scope);
                    Log.e("AUTH LOG", "Got Token: "+token);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                if (token != null) {
                    if(DataSet.setInstance()) {
                        DataSet.setToken(token);
                        DataSet.setUser(user);
                        if (DataSet.initFirebase()) {
                            updateUI();
                        }
                    }
                } else if (errorMessage != null) {
                }
            }
        };
        task.execute();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("AUTH LOG", "Getting OAuth Intent Results");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.e("AUTH LOG", "Got OAuth Intent Result");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct != null) {
                Log.e("AUTH LOG", "Account wasnt null");
                User user = new User(acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl());
                getGoogleOAuthTokenAndLogin(user);
            }
            handleSignInResult(result);
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
        } else {
            acct = null;
        }
        updateUI();
        Log.e("HandleSign", "Handle");
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Log.e("AUTH LOG", "Starting Logging in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Log.e("AUTH LOG", "Starting Log out");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        DataSet.setUser(null);
                      updateUI();
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
