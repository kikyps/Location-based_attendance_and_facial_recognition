package com.absensi.inuraini.admin.datapegawai;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.absensi.inuraini.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PegawaiRecyclerAdapter extends RecyclerView.Adapter<PegawaiRecyclerAdapter.MyViewHolder> implements Filterable {
    private final List<StoreDataPegawai> AllList;
    public List<StoreDataPegawai> FilteredList;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context;

    public PegawaiRecyclerAdapter(List<StoreDataPegawai> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_pegawai, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Collections.sort(AllList, StoreDataPegawai.storePegawaiComparator);
        StoreDataPegawai storeDataPegawai = AllList.get(position);
        databaseReference.child("DataJabatan").child(String.valueOf(storeDataPegawai.getsJabatan())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String jabatan = snapshot.child("sJabatan").getValue(String.class);
                    holder.tv_jabatan.setText(jabatan);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.tv_nama.setText(storeDataPegawai.getsNama());
        holder.tv_status.setText(storeDataPegawai.getsStatus());
        holder.card_view.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailPegawai.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("idPegawai", storeDataPegawai.getKey());
            context.startActivities(new Intent[]{intent});
        });
    }

    @Override
    public int getItemCount() {
        return AllList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String searchText = charSequence.toString().toLowerCase();
            List<StoreDataPegawai> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (StoreDataPegawai data : FilteredList) {
                    if (data.getsNama().toLowerCase().contains(searchText)) {
                        listFiltered.add(data);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = listFiltered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            AllList.clear();
            AllList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_nama, tv_jabatan, tv_status;
        CardView card_view;


        public MyViewHolder(@NonNull View iteView){
            super(iteView);

            tv_nama = iteView.findViewById(R.id.sNama);
            tv_jabatan = iteView.findViewById(R.id.sJabatan);
            tv_status = iteView.findViewById(R.id.sStatus);
            card_view = iteView.findViewById(R.id.card_view);
        }
    }
}
