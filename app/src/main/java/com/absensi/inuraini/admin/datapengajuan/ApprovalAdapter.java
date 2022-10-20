package com.absensi.inuraini.admin.datapengajuan;

import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.MyViewHolder> implements Filterable {
    private final List<DataApprove> AllList;
    public List<DataApprove> FilteredList;
    Context context;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public ApprovalAdapter(List<DataApprove> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_approval, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Collections.sort(AllList, DataApprove.dataApproveComparator);
        DataApprove dataIzin = AllList.get(position);
        String getTggl = dataIzin.getKey().substring(0, 2) + "/" + dataIzin.getKey().substring(2, 4) + "/" + dataIzin.getKey().substring(4, 8);
        holder.tv_tggl_izin.setText(getTggl);
        holder.tv_keterangan.setText(dataIzin.getsKet());
        if (dataIzin.issKonfirmAdmin()) {
            if (dataIzin.sAcc) {
                holder.tv_acc.setText("Diizinkan");
            } else {
                holder.tv_acc.setText("Ditolak");
            }
        } else {
            holder.tv_acc.setText("Menunggu Persetujuan");
        }
        holder.tv_tolak.setOnClickListener(v -> {
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sAcc", false);
            postValues.put("sKonfirmAdmin", true);
            databaseReference.child("user").child(ApprovalActivity.idIzin).child("sAbsensi").child(dataIzin.getKey())
                    .updateChildren(postValues)
                    .addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan periksa jaringan anda!", Toast.LENGTH_SHORT).show());
        });

        holder.tv_setuju.setOnClickListener(v -> {
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sAcc", true);
            postValues.put("sKonfirmAdmin", true);
            databaseReference.child("user").child(ApprovalActivity.idIzin).child("sAbsensi").child(dataIzin.getKey())
                    .updateChildren(postValues)
                    .addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan periksa jaringan anda!", Toast.LENGTH_SHORT).show());
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
            List<DataApprove> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (DataApprove data : FilteredList) {
                    if (data.getKey().contains(searchText)) {
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
        TextView tv_tggl_izin, tv_keterangan, tv_acc;
        Button tv_tolak, tv_setuju;
        CardView card_view;


        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            tv_tggl_izin = itemView.findViewById(R.id.sTggl_izin);
            tv_keterangan = itemView.findViewById(R.id.sKet_izin);
            tv_acc = itemView.findViewById(R.id.accorno);
            tv_tolak = itemView.findViewById(R.id.tolak_now);
            tv_setuju = itemView.findViewById(R.id.verif_now);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }
}
