package com.example.gopal.inventoryrealm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Gopal on 6/25/2019.
 */

public class ProductRecyclerApapter extends RecyclerView.Adapter<ProductRecyclerApapter.ProductViewHolder>{
    RealmResults<Product> products;
    Context mContext;
    int number;
    Realm realm;

    public ProductRecyclerApapter(Context context,RealmResults<Product> products1) {
        mContext = context;
        products = products1;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        ProductViewHolder viewHolder = new ProductViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        final int mPosition = position;

        Product product = products.get(position);
        holder.name.setText(product.getProductName());
        holder.price.setText(product.getProductPrice());
        holder.quantity.setText(String.valueOf(product.getProductQuantity()));
       /* if(product.getProductQuantity()!=null && product.getProductQuantity()!= "" ){
            number = Integer.valueOf(product.getProductQuantity());
        }
       number = product.getProductQuantity();*/
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saleButtonListener(holder,position);

            }
        });

        holder.rootview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,DetailsActivity.class);
                intent.putExtra("key",mPosition);
                mContext.startActivity(intent);
              //  mContext.startActivity(new Intent(mContext, DetailsActivity.class)).b

                Toast.makeText(mContext,"Clicked Position: " + mPosition,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saleButtonListener(ProductViewHolder viewHolder,int position){
        Product product = products.get(position);
        int quantity = product.getProductQuantity();
        quantity = quantity -1;
        viewHolder.quantity.setText(String.valueOf(quantity));
        realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        product.setProductQuantity(quantity);
        realm.commitTransaction();

    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public class ProductViewHolder extends RecyclerView.ViewHolder{
        TextView name,price,quantity;
        Button saleButton;
        LinearLayout rootview;

        public ProductViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.product_name);
            price = view.findViewById(R.id.price);
            quantity = view.findViewById(R.id.quantity);
            saleButton = view.findViewById(R.id.sale_button);
            rootview = view.findViewById(R.id.rootElement);

        }
    }
}
