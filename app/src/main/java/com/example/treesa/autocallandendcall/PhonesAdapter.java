package com.example.treesa.autocallandendcall;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class PhonesAdapter extends RecyclerView.Adapter {
        //Create list of notes
        List<Phones> phones = new ArrayList<>();
private MainActivity mainActivity ;
        PhoneListViewModel phoneListViewModel;

public PhonesAdapter(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        }
@NonNull
@Override
public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Get layout inflater
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //Inflate layout
        View row = inflater.inflate(R.layout.item_recycleview, parent, false);
        //return notes holder and pass row inside
        return new PhoneHolder(row);
        }

@Override
public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Get current Phones
        Phones currentPhones = phones.get(position);
        //cast notes holder
        PhoneHolder phoneHolder = (PhoneHolder) holder;
        //set title description and created at
        phoneHolder.phoneNum.setText(currentPhones.getNumber());

        //create random color and set it
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        ((PhoneHolder) holder).setItemclickListner(new ItemClickListner() {
        @Override
        public void onClick(View v, int position, boolean isLongClick) {
                    mainActivity.deleteItem(position);

            }});
        }

@Override
public int getItemCount() {
        return phones.size();
        }


public class PhoneHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,View.OnLongClickListener  {
    TextView phoneNum ;
    Button deleteBtn ;

    FrameLayout backStrip;
    private ItemClickListner  itemClickListner ;
    public PhoneHolder(View itemView) {
        super(itemView);
        phoneNum = itemView.findViewById(R.id.phone);
        deleteBtn = itemView.findViewById(R.id.delete);
        deleteBtn.setOnClickListener(this);
    }

    public void setItemclickListner(ItemClickListner itemClickListner){
        this.itemClickListner = itemClickListner ;
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition() ,false);

    }

    @Override
    public boolean onLongClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition() ,true);

        return true;
    }
}

    public void addPhones(List<Phones> phones) {
        this.phones = phones;
        notifyDataSetChanged();
    }



}
