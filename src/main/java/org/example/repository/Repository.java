package org.example.repository;

import org.example.model.Effort;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    List<Effort> efforts;

    public Repository() {
        this.efforts = new ArrayList<>();
    }

    public List<Effort> getEfforts() {
        return efforts;
    }

    public void addEffort(Effort effort) {
        efforts.add(effort);
    }

}
