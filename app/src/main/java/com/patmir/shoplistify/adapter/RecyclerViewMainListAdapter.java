package com.patmir.shoplistify.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.patmir.shoplistify.R;
import com.patmir.shoplistify.activity.MainActivity;
import com.patmir.shoplistify.activity.ViewListActivity;
import com.patmir.shoplistify.model.DataSet;
import com.patmir.shoplistify.model.ProductList;
import com.patmir.shoplistify.model.Settings;

import java.util.ArrayList;

/**
 * Created by Patryk on 21/03/2016.
 */
public class RecyclerViewMainListAdapter extends RecyclerView.Adapter<RecyclerViewMainListAdapter.MainListViewHolder> {
    private ArrayList<ProductList> data;
    private View parent;
    private ProductList backup;
    private int backupPos;
    private Settings settings;
    public RecyclerViewMainListAdapter(ArrayList<ProductList> data, Settings settings){
        this.data = data;
        this.settings = settings;
    }

    @Override
    public RecyclerViewMainListAdapter.MainListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_main_list_item, parent, false);
        this.parent = parent;
        MainListViewHolder holder = new MainListViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(MainListViewHolder holder, int position) {
        ProductList current = data.get(position);
        holder.vListName.setText(current.getName());
        int textApp = 0;
        switch (settings.getTextSize()){
            case 0:
                textApp = android.R.style.TextAppearance_Small;
                break;
            case 1:
                textApp = android.R.style.TextAppearance_Medium;
                break;
            case 2:
                textApp = android.R.style.TextAppearance_Large;
                break;
        }
        int cat = 0;
        switch (current.getCategory()){
            case 0:
                cat = R.drawable.ic_local_grocery_store;
                break;
            case 1:
                cat = R.drawable.ic_local_convenience_store;
                break;
            case 2:
                cat = R.drawable.ic_local_play;
                break;
        }
        holder.vCatIcon.setImageResource(cat);
        holder.vListName.setTextAppearance(textApp);
        holder.vDelBtn.getLayoutParams().height = holder.vListName.getLineHeight()*2;
        holder.vDelBtn.getLayoutParams().width = holder.vDelBtn.getLayoutParams().height;



    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }
    public void setData(ArrayList<ProductList> newData){
       this.data = new ArrayList<>(newData);
    }
    public ProductList removeItem(int position){
        final ProductList pL = data.remove(position);
        notifyItemRemoved(position);
        return pL;
    }
    public void addItem(int position, ProductList pL){
        data.add(position, pL);
        notifyItemInserted(position);
    }
    public void moveItem(int from, int to){
        final ProductList pl = data.remove(from);
        data.add(to, pl);
        notifyItemMoved(from, to);
    }
    public void animateTo(ArrayList<ProductList> pLs){
        applyAndAnimateRemovals(pLs);
        applyAndAnimateAdditions(pLs);
        applyAndAnimateMovedItems(pLs);
    }

    private void applyAndAnimateRemovals(ArrayList<ProductList> newPls){
        for (int i = data.size() -1; i >= 0; i--){
            final ProductList pL = data.get(i);
            if(!newPls.contains(pL)){
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(ArrayList<ProductList> newPls){
        for(int i = 0, count = newPls.size(); i < count; i++){
            final ProductList pL = newPls.get(i);
            if (!data.contains(pL)){
                addItem(i, pL);
            }
        }
    }
    private void applyAndAnimateMovedItems(ArrayList<ProductList> newPls){
        for(int to = newPls.size()-1; to >= 0; to--){
            final ProductList pL = newPls.get(to);
            final int from = data.indexOf(pL);
            if (from >= 0 && from != to){
                moveItem(from, to);
            }
        }
    }

    class MainListViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        protected  TextView vListName;
        private Context context;
        protected ImageView vDelBtn;
        protected ImageView vCatIcon;
        private int pos;

        public MainListViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            vListName = (TextView)itemView.findViewById(R.id.recycler_view_list_name);
            vDelBtn = (ImageView) itemView.findViewById(R.id.delete_list_item_btn);
            vDelBtn.setOnClickListener(this);
            vCatIcon = (ImageView) itemView.findViewById(R.id.list_item_category);
        }

        @Override
        public boolean onLongClick(final View v) {
            final CharSequence[] items = {"Edit", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setItems(items, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) { // Edit
                        openEditListItemDialog(v, vListName, getAdapterPosition());
                    } else { // Delete
                        vDelBtn.callOnClick();

                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }

        @Override
        public void onClick(final View view) {
            switch(view.getId()){
                case R.id.recycler_view_list_wrapper:
                    final Intent intent;
                    intent = new Intent(context, ViewListActivity.class);
                    intent.putExtra("data_position", getAdapterPosition());
                    context.startActivity(intent);
                    break;
                case R.id.delete_list_item_btn:
                    pos = getAdapterPosition();
                    new AlertDialog.Builder(view.getContext())
                            .setTitle(R.string.delete_item)
                            .setMessage(R.string.delete_item_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    saveBackup(pos);
                                    removeItem(pos);
                                    Snackbar snackbar = Snackbar
                                            .make(parent, R.string.item_deleted, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.undo, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    addItem(backupPos, backup);
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
                    ((MainActivity)parent.getContext()).updateUI();
                    DataSet.getInstance().saveCache();
                    break;
            }
        }
    }
    public void saveBackup(int pos) {
        backupPos = pos;
        backup = data.get(pos);
    }
    public void openEditListItemDialog(final View v, final TextView vListName, final int position){
        AlertDialog.Builder builder =  new AlertDialog.Builder(v.getContext());
        LayoutInflater factory = LayoutInflater.from(v.getContext());
        final View new_list_dialog_view = factory.inflate(R.layout.add_new_list_dialog, null);
        builder.setView(new_list_dialog_view);
        builder.setTitle(R.string.edit_list_title)
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
        EditText name = (EditText) new_list_dialog_view.findViewById(R.id.new_list_name);
        name.setText(vListName.getText());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                EditText name = (EditText) new_list_dialog_view.findViewById(R.id.new_list_name);
                AppCompatSpinner cat = (AppCompatSpinner) new_list_dialog_view.findViewById(R.id.list_category_spinner);
                if (name.getText().toString().trim().length() > 0) {

                      data.get(position).setName(name.getText().toString());
                    data.get(position).setCategory(cat.getSelectedItemPosition());
                        notifyItemChanged(position);
                        DataSet.getInstance().saveCache();
                        wantToCloseDialog = true;

                } else {
                    name.setError("You must give a name.");
                }
                if (wantToCloseDialog)
                    dialog.dismiss();
            }
        });

    }
}
