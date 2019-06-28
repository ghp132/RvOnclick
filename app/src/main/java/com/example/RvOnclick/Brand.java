package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Brand {
    @PrimaryKey
    @NonNull
    private String brandName;

    @NonNull
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(@NonNull String brandName) {
        this.brandName = brandName;
    }
}
