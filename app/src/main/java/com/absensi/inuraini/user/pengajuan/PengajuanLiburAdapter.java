package com.absensi.inuraini.user.pengajuan;

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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.admin.verifyaccount.DetailVerifAccount;
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

public class PengajuanLiburAdapter extends RecyclerView.Adapter<PengajuanLiburAdapter.MyViewHolder> implements Filterable {
    private final List<DataIzin> AllList;
    public List<DataIzin> FilteredList;
    Context context;

    public PengajuanLiburAdapter(List<DataIzin> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_req_izin, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PengajuanLiburAdapter.MyViewHolder holder, int position) {
        Collections.sort(AllList, DataIzin.dataIzinComparator);
        DataIzin dataIzin = AllList.get(position);
        String getTggl = dataIzin.getKey().substring(0, 2) + "/" + dataIzin.getKey().substring(2, 4) + "/" + dataIzin.getKey().substring(4, 8);
        holder.tv_tggl_izin.setText(getTggl);
        holder.tv_keterangan.setText(dataIzin.getsKet());
        if (dataIzin.issKonfirmAdmin()) {
            if (dataIzin.sAcc) {
                holder.tv_acc.setText("Diizinkan");
                holder.tv_acc.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.tv_acc.setText("Ditolak");
                holder.tv_acc.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        } else {
            holder.tv_acc.setText("Menunggu Persetujuan");
        }
        holder.card_view.setOnClickListener(view -> {
            if (dataIzin.issKonfirmAdmin()){
                Toast.makeText(context, "Permintaan libur anda telah di tanggapi oleh admin anda tidak dapat mengedit atau membatalkan permintaan!", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(context, TambahIzinActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("updateIzin", true);
                intent.putExtra("setTggl", holder.tv_tggl_izin.getText().toString());
                intent.putExtra("setKet", holder.tv_keterangan.getText().toString());
                intent.putExtra("idIzin", dataIzin.getKey());
                context.startActivities(new Intent[]{intent});
            }
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
            List<DataIzin> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (DataIzin data : FilteredList) {
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
        CardView card_view;


        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            tv_tggl_izin = itemView.findViewById(R.id.sTggl_izin);
            tv_keterangan = itemView.findViewById(R.id.sKet_izin);
            tv_acc = itemView.findViewById(R.id.accorno);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }
}
