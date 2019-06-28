package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Territory {
    @PrimaryKey
    @NonNull
    private String territoryName;
    private boolean isGroup;
    private String parentTerritory;
    private boolean disabled;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @NonNull
    public String getTerritoryName() {
        return territoryName;
    }

    public void setTerritoryName(@NonNull String territoryName) {
        this.territoryName = territoryName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getParentTerritory() {
        return parentTerritory;
    }

    public void setParentTerritory(String parentTerritory) {
        this.parentTerritory = parentTerritory;
    }
}
