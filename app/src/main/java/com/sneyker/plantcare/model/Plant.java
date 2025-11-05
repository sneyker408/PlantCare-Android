package com.sneyker.plantcare.model;

public class Plant {
    private String id;
    private String name;
    private String species;
    private int freqDays;       // frecuencia de riego (días)
    private long lastWatered;   // millis (UTC)
    private long nextWater;     // millis (UTC)
    private String notes;
    private String photoUrl;    // opcional

    public Plant() {} // requerido por Firestore

    public Plant(String id, String name, String species, int freqDays,
                 long lastWatered, long nextWater, String notes, String photoUrl) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.freqDays = freqDays;
        this.lastWatered = lastWatered;
        this.nextWater = nextWater;
        this.notes = notes;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public int getFreqDays() { return freqDays; }
    public void setFreqDays(int freqDays) { this.freqDays = freqDays; }

    public long getLastWatered() { return lastWatered; }
    public void setLastWatered(long lastWatered) { this.lastWatered = lastWatered; }

    public long getNextWater() { return nextWater; }
    public void setNextWater(long nextWater) { this.nextWater = nextWater; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
