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

import io.realm.RealmResults;

/**
 * Created by Gopal on 6/25/2019.
 */

public class ProductRecyclerApapter extends RecyclerView.Adapter<ProductRecyclerApapter.ProductViewHolder>{
    RealmResults<Product> products;
    Context mContext;
    int number;

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
    public void onBindViewHolder(final ProductViewHolder holder, int position) {

        Product product = products.get(position);
        holder.name.setText(product.getProductName());
        holder.price.setText(product.getProductPrice());
        holder.quantity.setText(String.valueOf(product.getProductQuantity()));
       /* if(product.getProductQuantity()!=null && product.getProductQuantity()!= "" ){
            number = Integer.valueOf(product.getProductQuantity());
        }*/
       number = product.getProductQuantity();
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = number - 1;
                holder.quantity.setText(String.valueOf(number));
            }
        });

//        holder.rootview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mContext.startActivity(new Intent(mContext,DetailsActivity.class));
//            }
//        });

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
            rootview = view.findViewById(R.id.rootView);

        }
    }
}
