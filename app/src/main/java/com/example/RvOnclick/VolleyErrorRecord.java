package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class VolleyErrorRecord {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long errorId;
    private String orgin;
    private String timeStamp;
    private String errorBody;

    @NonNull
    public Long getErrorId() {
        return errorId;
    }

    public void setErrorId(@NonNull Long errorId) {
        this.errorId = errorId;
    }

    public String getOrgin() {
        return orgin;
    }

    public void setOrgin(String orgin) {
        this.orgin = orgin;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }
}
