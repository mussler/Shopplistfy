package com.patmir.shoplistify.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.patmir.shoplistify.R;
import com.patmir.shoplistify.model.DataSet;
import com.patmir.shoplistify.model.Product;
import com.patmir.shoplistify.model.ProductList;
import com.patmir.shoplistify.model.Settings;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Created by Patryk on 21/03/2016.
 */
public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.ListViewHolder> {
    private ProductList productList;
    private View parent;
    private Product backup;
    private int backupPos;
    private Settings settings;

    public RecyclerViewListAdapter(ProductList data, Settings settings){
        this.productList = data;
        this.settings = settings;
    }

    @Override
    public RecyclerViewListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_item, parent, false);

        ListViewHolder holder = new ListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Product current = productList.getProduct(position);
        holder.vListName.setText(current.getName());
        holder.vListQuantity.setText((String.valueOf(current.getQuantity())));
        holder.vCheckBox.setChecked(current.getCheckBox());

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
        holder.vListName.setTextAppearance(textApp);
        holder.vListQuantity.setTextAppearance(textApp);
        holder.vDelBtn.getLayoutParams().height = holder.vListName.getLineHeight()*2;
        holder.vDelBtn.getLayoutParams().width = holder.vDelBtn.getLayoutParams().height;

    }

    @Override
    public int getItemCount() {
        return productList.getSize();
    }

    public void refresh() {
        notifyDataSetChanged();
    }
    public void setData(ProductList newData){
        this.productList = new ProductList(newData.getName(), newData.getCategory());
        productList.addAll(newData.getProducts());
    }
    public Product removeItem(int position){
        final Product p = productList.removeProduct(position);
        notifyItemRemoved(position);
        return p;
    }
    public void addItem(int position, Product p){
        productList.addProduct(p, position);
        notifyItemInserted(position);
    }
    public void moveItem(int from, int to){
        final Product p = productList.removeProduct(from);
        productList.addProduct(p, to);
        notifyItemMoved(from, to);
    }
    public void animateTo(ProductList pL){
        applyAndAnimateRemovals(pL);
        applyAndAnimateAdditions(pL);
        applyAndAnimateMovedItems(pL);
    }

    private void applyAndAnimateRemovals(ProductList newPl){
        for (int i = productList.getSize() -1; i >= 0; i--){
            final Product p = productList.getProduct(i);
            if(!newPl.getProducts().contains(p)){
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(ProductList newPl){
        for(int i = 0, count = newPl.getSize(); i < count; i++){
            final Product p = newPl.getProduct(i);
            if (!productList.getProducts().contains(p)){
                addItem(i, p);
            }
        }
    }
    private void applyAndAnimateMovedItems(ProductList newPl){
        for(int to = newPl.getSize()-1; to >= 0; to--){
            final Product p = newPl.getProduct(to);
            final int from = productList.getProducts().indexOf(p);
            if (from >= 0 && from != to){
                moveItem(from, to);
            }
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected  TextView vListName;
        protected  TextView vListQuantity;
        protected CheckBox vCheckBox;
        protected ImageView vDelBtn;

        public ListViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            vListName = (TextView)itemView.findViewById(R.id.list_view_name);
            vListQuantity = (TextView)itemView.findViewById(R.id.list_view_quantity);
            vCheckBox = (CheckBox) itemView.findViewById(R.id.checkBox_list_item);
            vDelBtn = (ImageView) itemView.findViewById(R.id.delete_list_item_btn);
            vDelBtn.setOnClickListener(this);

            vCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        productList.getProduct(getAdapterPosition()).setCheckBox(b);
                    DataSet.saveCache();
                }
            });
        }
        @Override
        public void onClick(final View v) {
            switch(v.getId()) {
                //case R.id.checkBox_list_item:
                case R.id.list_view_wrapper:
                   this.vCheckBox.toggle();
                    break;
                case R.id.delete_list_item_btn:

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.delete_item)
                            .setMessage(R.string.delete_item_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    saveBackup(getAdapterPosition());
                                    productList.removeProduct(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    DataSet.saveCache();
                                    final View parent = v;
                                    Snackbar snackbar = Snackbar
                                            .make(parent, R.string.item_deleted, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.undo, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                   productList.addProduct(backup, backupPos);
                                                    notifyItemInserted(backupPos);
                                                    DataSet.saveCache();
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



                    break;
            }
            }



        public void saveBackup(int pos) {
            backupPos = pos;
            backup = productList.getProduct(pos);
        }

        @Override
        public boolean onLongClick(final View v) {
            final CharSequence[] items = {"Edit", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setItems(items, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) { // Edit
                        openEditListItemDialog(v, vListName, vListQuantity, getAdapterPosition());
                    } else { // Delete
                        vDelBtn.callOnClick();

                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
    }
    public void openEditListItemDialog(final View v, final TextView vListName, final TextView vListQuantity, final int position){
        AlertDialog.Builder builder =  new AlertDialog.Builder(v.getContext());
        LayoutInflater factory = LayoutInflater.from(v.getContext());
        final View new_list_dialog_view = factory.inflate(R.layout.add_edit_new_list_item_dialog, null);
        builder.setView(new_list_dialog_view);
        builder.setTitle(R.string.edit_list_item_title)
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
        EditText name = (EditText) new_list_dialog_view.findViewById(R.id.list_item_name);
        EditText quantity = (EditText) new_list_dialog_view.findViewById(R.id.list_item_quantity);
        name.setText(vListName.getText());
        quantity.setText(vListQuantity.getText());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                EditText name = (EditText) new_list_dialog_view.findViewById(R.id.list_item_name);
                EditText quantity = (EditText) new_list_dialog_view.findViewById(R.id.list_item_quantity);
                if (name.getText().toString().trim().length() > 0) {
                    if(quantity.getText().toString().trim().length() > 0) {

                        productList.getProduct(position).setName(name.getText().toString());
                        productList.getProduct(position).setQuantity(Integer.parseInt(quantity.getText().toString()));
                        notifyItemChanged(position);
                            DataSet.getInstance().saveCache();
                        wantToCloseDialog = true;
                    } else {
                        quantity.setError("You must give a number");
                    }
                } else {
                    name.setError("You must give a name.");
                }
                if (wantToCloseDialog)
                    dialog.dismiss();
            }
        });

    }
}
