package com.patmir.shoplistify.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.support.v7.widget.AppCompatSpinner;
import android.widget.CompoundButton;

import com.patmir.shoplistify.R;
import com.patmir.shoplistify.model.DataSet;
import com.patmir.shoplistify.model.Settings;

public class SettingsActivity extends AppCompatActivity implements AppCompatSpinner.OnItemSelectedListener, SwitchCompat.OnCheckedChangeListener {

    private DataSet dataSet;
    private Settings settings;
    private AppCompatSpinner mSpinner;
    private SwitchCompat mSwitch;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        if(DataSet.getInstance() != null){
            dataSet = DataSet.getInstance();
            settings = dataSet.getSettings();
        }

        mSpinner = (AppCompatSpinner) findViewById(R.id.settings_text_size_spinner);
        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setSelection(settings.getTextSize());
        mSwitch = (SwitchCompat) findViewById(R.id.settings_sync_switch);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(settings.getSync());


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        settings.setTextSize(i);
        DataSet.setSettings(settings);
        dataSet.saveCache();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        settings.setSync(b);
        DataSet.setSettings(settings);
        dataSet.saveCache();
    }
}
