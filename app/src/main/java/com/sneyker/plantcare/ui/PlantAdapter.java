package com.sneyker.plantcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sneyker.plantcare.R;
import com.sneyker.plantcare.model.Plant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.VH> {

    public interface Listener {
        void onEdit(Plant p);
        void onDelete(Plant p);
    }

    private final List<Plant> data = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PlantAdapter(Listener listener) { this.listener = listener; }

    public void setData(List<Plant> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Plant p = data.get(position);
        h.txtName.setText(p.getName());
        h.txtSpecies.setText(p.getSpecies());
        h.txtNext.setText(p.getNextWater() > 0
                ? "Próximo riego: " + df.format(new Date(p.getNextWater()))
                : "Próximo riego: —");

        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtSpecies, txtNext;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtSpecies = itemView.findViewById(R.id.txtSpecies);
            txtNext = itemView.findViewById(R.id.txtNext);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
