package com.absensi.inuraini.admin.datapengajuan;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class DataPengajuanAdapter extends RecyclerView.Adapter<DataPengajuanAdapter.MyViewHolder> implements Filterable {
    private final List<DataReqIzin> AllList;
    public List<DataReqIzin> FilteredList;
    ArrayList<DataFilterIzin> listFilterIzin = new ArrayList<>();
    Context context;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    String jabatan;
    public static String countIzin;

    public DataPengajuanAdapter(ArrayList<DataReqIzin> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_data_izin, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataReqIzin storeUser = AllList.get(position);
        databaseReference.child("user").child(storeUser.getKey()).child("sAbsensi").orderByChild("sKehadiran").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listFilterIzin = new ArrayList<>();
                if (snapshot.exists()){
                    for (DataSnapshot item : snapshot.getChildren()) {
                        DataFilterIzin filterIzin = item.getValue(DataFilterIzin.class);
                        if (!filterIzin.issKonfirmAdmin()){
                            storeUser.setsKehadiran(false);
                            storeUser.setsKonfirmAdmin(false);
                            storeUser.setSket(filterIzin.getsKet());
                        } else {
                            storeUser.setsKonfirmAdmin(true);
                        }
                    }
                } else {
                    storeUser.setsKehadiran(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("DataJabatan").child(String.valueOf(storeUser.getsJabatan())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    jabatan = snapshot.child("sJabatan").getValue(String.class);
                    holder.tv_ketizin.setText(jabatan);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tv_nama.setText(storeUser.getsNama());

        Runnable runnable = () -> sortItem();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 1000);

        holder.card_view.setOnClickListener(view -> {
            Intent intent = new Intent(context, ApprovalActivity.class);
            intent.putExtra("idIzin", storeUser.getKey());
            intent.putExtra("getNama", storeUser.getsNama());
            intent.putExtra("getJabatan", jabatan);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            List<DataReqIzin> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (DataReqIzin data : FilteredList) {
                    if (!data.issKehadiran()) {
                        if (!data.issKonfirmAdmin()) {
                            listFiltered.add(data);
                        }
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

    public void sortItem() {
        List<DataReqIzin> listFiltered = new ArrayList<>();
        DataReqIzin top = null;
        for(DataReqIzin data : FilteredList){
            if (!data.issKehadiran()) {
                if (!data.issKonfirmAdmin()) {
                    listFiltered.add(data);
                }
            }
        }
        if(top != null){
            listFiltered.add(0, top);
        }
        AllList.clear();
        AllList.addAll(listFiltered);
        notifyDataSetChanged();
        countIzin = String.valueOf(listFiltered.size());
//        Toast.makeText(context, String.valueOf(listFiltered.size()), Toast.LENGTH_SHORT).show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_nama, tv_ketizin, tv_izin;
        CardView card_view;

        public MyViewHolder(@NonNull View iteView){
            super(iteView);

            tv_nama = iteView.findViewById(R.id.txt_nama);
            tv_ketizin = iteView.findViewById(R.id.txt_jabatan);
            tv_izin = iteView.findViewById(R.id.izin_txt);
            card_view = iteView.findViewById(R.id.card_view);
        }
    }
}
