package com.absensi.inuraini.admin.jabatan;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JabatanRecyclerAdapter extends RecyclerView.Adapter<JabatanRecyclerAdapter.MyViewHolder> implements Filterable {
    private final List<StoreJabatan> AllList;
    public List<StoreJabatan> FilteredList;
    Context context;

    public JabatanRecyclerAdapter(List<StoreJabatan> mList, Context context) {
        this.context = context;
        this.AllList = mList;
        //this.FilteredList = mList;
        FilteredList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_jabatan, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Collections.sort(AllList, StoreJabatan.storejabatanComparator);
        StoreJabatan storeJabatan = AllList.get(position);
        holder.tv_Jabatan.setText(storeJabatan.getsJabatan());
        holder.card_view.setOnClickListener(view -> {
            Intent intent = new Intent(context, TambahJabatan.class);
            intent.putExtra("update", true);
            intent.putExtra("idJabatan", storeJabatan.getKey());
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
            List<StoreJabatan> listFiltered = new ArrayList<>();

            if (searchText.isEmpty()) {
                listFiltered.addAll(FilteredList);
            } else {
                for (StoreJabatan data : FilteredList) {
                    if (data.getsJabatan().toLowerCase().contains(searchText)) {
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
        TextView tv_Jabatan;
        CardView card_view;


        public MyViewHolder(@NonNull View iteView){
            super(iteView);

            tv_Jabatan = iteView.findViewById(R.id.sJabatan);
            card_view = iteView.findViewById(R.id.card_view);
        }
    }
}
