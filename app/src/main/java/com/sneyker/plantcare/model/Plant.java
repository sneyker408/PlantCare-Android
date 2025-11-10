package com.sneyker.plantcare.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Plant {

    private String id;
    private String name;
    private String species;
    private int freqDays;
    private Object lastWatered;  // CAMBIO: Acepta Object para manejar Long o Timestamp
    private String notes;

    public Plant() {
        // Constructor vacío requerido por Firebase
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public int getFreqDays() {
        return freqDays;
    }

    public void setFreqDays(int freqDays) {
        this.freqDays = freqDays;
    }

    /**
     * Obtiene lastWatered como Timestamp
     * Compatible con valores Long (timestamp) o Timestamp de Firebase
     */
    @Exclude
    public Timestamp getLastWatered() {
        if (lastWatered == null) {
            return null;
        }

        // Si ya es Timestamp, devolverlo directamente
        if (lastWatered instanceof Timestamp) {
            return (Timestamp) lastWatered;
        }

        // Si es Long (formato antiguo), convertirlo a Timestamp
        if (lastWatered instanceof Long) {
            long millis = (Long) lastWatered;
            return new Timestamp(millis / 1000, 0);
        }

        return null;
    }

    /**
     * Establece lastWatered (acepta Timestamp)
     */
    public void setLastWatered(Timestamp timestamp) {
        this.lastWatered = timestamp;
    }

    /**
     * Método usado por Firestore para serializar
     */
    public Object getLastWateredRaw() {
        return lastWatered;
    }

    /**
     * Método usado por Firestore para deserializar
     */
    public void setLastWateredRaw(Object value) {
        this.lastWatered = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}