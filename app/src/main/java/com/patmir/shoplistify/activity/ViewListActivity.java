package com.patmir.shoplistify.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.TextView;

import com.patmir.shoplistify.R;
import com.patmir.shoplistify.adapter.RecyclerViewListAdapter;
import com.patmir.shoplistify.adapter.RecyclerViewMainListAdapter;
import com.patmir.shoplistify.model.DataSet;
import com.patmir.shoplistify.model.Product;
import com.patmir.shoplistify.model.ProductList;
import com.patmir.shoplistify.model.Settings;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class ViewListActivity extends AppCompatActivity implements  View.OnClickListener, View.OnLongClickListener{
    private Toolbar mToolbar;
    private ProductList data;
    private RecyclerView recyclerView;
    private RecyclerViewListAdapter recyclerViewListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private DataSet dataSet;
    private ProductList backup;
    private boolean mSearchCheck;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        //Menu slide-in
        //Instance save
        Bundle bundle = getIntent().getExtras();
        int position = bundle.getInt("data_position");
            dataSet = DataSet.getInstance();
            data = DataSet.getData().get(position);
            settings = DataSet.getSettings();
        getSupportActionBar().setTitle(data.getName());
        recyclerViewListAdapter = new RecyclerViewListAdapter(data, settings);
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(recyclerViewListAdapter);
        recyclerView.setLayoutManager(layoutManager);

        //Listeners
        findViewById(R.id.new_list_item_btn).setOnClickListener(this);
        updateUI();
    }
    public void updateUI(){
        TextView empty_info = (TextView) findViewById(R.id.list_empty_info);
        if(data.getSize() == 0){
            empty_info.setVisibility(View.VISIBLE);
        } else {
            empty_info.setVisibility(View.GONE);
            recyclerViewListAdapter.setData(data);
            recyclerViewListAdapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
        }
        recyclerViewListAdapter.refresh();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_list_item_btn:
                openNewListItemDialog(v);
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable("savedBag", data);
    }
    protected void onRestoreInstanceState(Bundle savedState){
        super.onRestoreInstanceState(savedState);
        this.data = savedState.getParcelable("savedBag");
    }
    public void openNewListItemDialog(final View v){
        AlertDialog.Builder builder =  new AlertDialog.Builder(v.getContext());
        LayoutInflater factory = LayoutInflater.from(v.getContext());
        final View new_list_item_dialog_view = factory.inflate(R.layout.add_edit_new_list_item_dialog, null);
        builder.setView(new_list_item_dialog_view);
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
                EditText name = (EditText) new_list_item_dialog_view.findViewById(R.id.list_item_name);
                EditText quantity = (EditText)new_list_item_dialog_view.findViewById(R.id.list_item_quantity);
                if (name.getText().toString().trim().length() > 0) {
                    if(quantity.getText().toString().trim().length() > 0) {
                        int position = data.getSize();
                        data.addProduct(new Product(name.getText().toString(), Integer.parseInt(quantity.getText().toString())), 0);
                        recyclerViewListAdapter.notifyItemInserted(0);
                        updateUI();
                        dataSet.saveCache();
                        wantToCloseDialog = true;
                    } else {
                        quantity.setError("You must give a quantity.");
                    }
                } else {
                    name.setError("You must give a name.");
                }
                if (wantToCloseDialog)
                    dialog.dismiss();
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_list, menu);
        //Select search item
        final MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.setVisible(true);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(this.getString(R.string.search));

        ((EditText) searchView.findViewById(R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.white));
        searchView.setOnQueryTextListener(onQuerySearchView);

        menu.findItem(R.id.menu_delete).setVisible(true);

        mSearchCheck = true;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_delete:
                new AlertDialog.Builder(recyclerView.getContext())
                        .setTitle(R.string.clear_list)
                        .setMessage(R.string.clear_list_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                backup = new ProductList(data.getName());
                                backup.addAll(data.getProducts());
                                data.clearAll();
                                updateUI();
                                recyclerViewListAdapter.notifyDataSetChanged();
                                dataSet.saveCache();
                                final View parent = recyclerView; //?
                                Snackbar snackbar = Snackbar
                                        .make(parent, R.string.item_deleted, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.undo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                data.addAll(backup.getProducts());
                                                updateUI();
                                                recyclerViewListAdapter.notifyDataSetChanged();
                                                dataSet.saveCache();
                                                Snackbar snackbar = Snackbar.make(view, R.string.item_restored, Snackbar.LENGTH_SHORT);
                                                snackbar.show();
                                            }
                                        });
                                snackbar.show();

                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private SearchView.OnQueryTextListener onQuerySearchView = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }
        @Override
        public boolean onQueryTextChange(String s) {
            if (mSearchCheck) {
                if (s.length() > 0) {
                    final ProductList filteredData = filter(data, s);
                    recyclerViewListAdapter.animateTo(filteredData);

                } else {
                    recyclerViewListAdapter.animateTo(data);
                }
                recyclerView.scrollToPosition(0);
            }
            return false;
        }
    };
    private ProductList filter(ProductList pL, String s){
        s = s.toLowerCase();
        final ProductList filteredPl = new ProductList(pL.getName(), pL.getCategory());
        for (Product p : pL.getProducts()){
            final String text = p.getName().toLowerCase();
            if(text.contains(s)){
                filteredPl.addProduct(p);
            }
        }
        return filteredPl;
    }
    private SearchView.OnCloseListener onCloseListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {

            recyclerViewListAdapter.animateTo(data);
            recyclerView.scrollToPosition(0);
            updateUI();
            return false;
        }
    };
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        this.recyclerViewListAdapter.refresh();
    }

}
