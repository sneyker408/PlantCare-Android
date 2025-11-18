package com.sneyker.plantcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.model.Plant;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlantAdapter extends ListAdapter<Plant, PlantAdapter.PlantViewHolder> {

    private Listener listener;

    public interface Listener {
        void onPlantClick(Plant plant);
        void onEditPlant(Plant plant);
        void onDeletePlant(Plant plant);
        void onWaterNow(Plant plant);
    }

    public PlantAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Plant> DIFF_CALLBACK = new DiffUtil.ItemCallback<Plant>() {
        @Override
        public boolean areItemsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getSpecies().equals(newItem.getSpecies());
        }
    };

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = getItem(position);
        holder.bind(plant, listener);
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtSpecies;
        private TextView txtNext;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private Button btnWater;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtSpecies = itemView.findViewById(R.id.txtSpecies);
            txtNext = itemView.findViewById(R.id.txtNext);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnWater = itemView.findViewById(R.id.btnWater);
        }

        public void bind(Plant plant, Listener listener) {
            // Nombre de la planta
            if (plant.getName() != null) {
                txtName.setText(plant.getName());
            } else {
                txtName.setText("Sin nombre");
            }

            // Especie
            if (plant.getSpecies() != null) {
                txtSpecies.setText(plant.getSpecies());
            } else {
                txtSpecies.setText("Sin especie");
            }

            // Calcular próximo riego
            String nextWatering = getNextWater(plant);
            txtNext.setText(nextWatering);

            // Eventos de clic
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlantClick(plant);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditPlant(plant);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePlant(plant);
                }
            });

            btnWater.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWaterNow(plant);
                }
            });
            // Ubicación
            TextView txtLocation = itemView.findViewById(R.id.txtLocation);
            if (plant.getLocation() != null && !plant.getLocation().isEmpty()) {
                txtLocation.setVisibility(View.VISIBLE);
                txtLocation.setText("📍 " + plant.getLocation());
            } else {
                txtLocation.setVisibility(View.GONE);
            }

// Nivel de luz
            TextView txtLight = itemView.findViewById(R.id.txtLight);
            if (plant.getLightLevel() != null) {
                txtLight.setVisibility(View.VISIBLE);
                String icon = plant.getLightLevel().equals("Alta") ? "☀️" :
                        plant.getLightLevel().equals("Baja") ? "🌑" : "🌤️";
                txtLight.setText(icon + " " + plant.getLightLevel());
            } else {
                txtLight.setVisibility(View.GONE);
            }

// Tamaño
            TextView txtSize = itemView.findViewById(R.id.txtSize);
            if (plant.getSize() != null) {
                txtSize.setVisibility(View.VISIBLE);
                String icon = plant.getSize().equals("Pequeña") ? "🌱" :
                        plant.getSize().equals("Grande") ? "🌳" : "🪴";
                txtSize.setText(icon + " " + plant.getSize());
            } else {
                txtSize.setVisibility(View.GONE);
            }

        }

        private String getNextWater(Plant plant) {
            if (plant == null) {
                return "Sin información";
            }

            Timestamp lastWatered = plant.getLastWatered();
            int freqDays = plant.getFreqDays();

            if (lastWatered == null) {
                return "Nunca regada";
            }

            try {
                // Convertir Timestamp a Date
                Date lastWateredDate = lastWatered.toDate();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lastWateredDate);
                calendar.add(Calendar.DAY_OF_MONTH, freqDays);

                Date nextDate = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                return "Próximo: " + sdf.format(nextDate);
            } catch (Exception e) {
                return "Error al calcular";
            }
        }
    }
}