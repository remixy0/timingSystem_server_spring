package org.example.service;

import org.example.model.Effort;
import org.example.repository.Repository;

import java.util.List;

public class Service {
    Repository repository;

    public Service() {
        this.repository = new Repository();
    }

    public List<Effort> getEfforts() {
        return this.repository.getEfforts();
    }

    public void addEffort(Effort effort) {
        repository.addEffort(effort);
    }


}
