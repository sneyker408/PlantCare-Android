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
    private Object lastWatered;
    private String notes;

    // NUEVOS CAMPOS
    private String photoUrl;
    private String location; // Sala, Cocina, Balcón, etc.
    private String lightLevel; // Baja, Media, Alta
    private String humidity; // Baja, Media, Alta
    private boolean notifications;
    private String size; // Pequeña, Mediana, Grande
    private Timestamp createdAt;

    public Plant() {
        // Constructor vacío requerido por Firebase
        this.notifications = true;
        this.createdAt = Timestamp.now();
    }

    // Getters y Setters existentes

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

    @Exclude
    public Timestamp getLastWatered() {
        if (lastWatered == null) {
            return null;
        }

        if (lastWatered instanceof Timestamp) {
            return (Timestamp) lastWatered;
        }

        if (lastWatered instanceof Long) {
            long millis = (Long) lastWatered;
            return new Timestamp(millis / 1000, 0);
        }

        return null;
    }

    public void setLastWatered(Timestamp timestamp) {
        this.lastWatered = timestamp;
    }

    public Object getLastWateredRaw() {
        return lastWatered;
    }

    public void setLastWateredRaw(Object value) {
        this.lastWatered = value;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // NUEVOS Getters y Setters

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(String lightLevel) {
        this.lightLevel = lightLevel;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}