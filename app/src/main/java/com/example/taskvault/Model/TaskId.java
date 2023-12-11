package com.example.taskvault.Model;

import androidx.annotation.NonNull;

public class TaskId {
    public String TaskId;

    public  <T extends  TaskId> T withId(@NonNull final String id){
        this.TaskId = id;
        return  (T) this;
    }
}
