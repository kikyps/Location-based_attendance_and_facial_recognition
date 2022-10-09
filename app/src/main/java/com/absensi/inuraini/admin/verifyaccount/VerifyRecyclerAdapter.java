package com.absensi.inuraini.admin.verifyaccount;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.datapegawai.DetailPegawai;
import com.absensi.inuraini.admin.datapegawai.StoreDataPegawai;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyRecyclerAdapter extends RecyclerView.Adapter<VerifyRecyclerAdapter.MyViewHolder> implements Filterable {
    private final List<DataVerify> AllList;
    public List<DataVerify> FilteredList;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context;

    public VerifyRecyclerAdapter(List<DataVerify> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_verif, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Collections.sort(AllList, DataVerify.dataVerifyComparator);
        DataVerify dataVerify = AllList.get(position);
        databaseReference.child("DataJabatan").child(String.valueOf(dataVerify.getsJabatan())).addValueEventListener(new ValueEventListener() {
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
        holder.tv_nama.setText(dataVerify.getsNama());
        holder.btn_verif.setOnClickListener(v -> {
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sVerified", true);
            databaseReference.child("user").child(dataVerify.getKey()).updateChildren(postValues);
            Toast.makeText(context, dataVerify.getsNama() + " diverifikasi", Toast.LENGTH_SHORT).show();
        });
        holder.card_view.setOnClickListener(view -> {
//            Toast.makeText(context, dataVerify.getsNama(), Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(context, DetailPegawai.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("idPegawai", dataVerify.getKey());
//            context.startActivities(new Intent[]{intent});
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
            List<DataVerify> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (DataVerify data : FilteredList) {
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
        TextView tv_nama, tv_jabatan;
        Button btn_verif;
        CardView card_view;


        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            tv_nama = itemView.findViewById(R.id.sNama);
            tv_jabatan = itemView.findViewById(R.id.sJabatan);
            btn_verif = itemView.findViewById(R.id.verif_now);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }
}
