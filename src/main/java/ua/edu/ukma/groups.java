package ua.edu.ukma;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class groups {
    @Id
    private int id_group;
    private String name;
    private String description;

    public groups() {
        id_group = 0;
        name = "None";
        description = "None";
    }

    public int getId_group() {
        return id_group;
    }

    public void setId_group(int id_group) {
        this.id_group = id_group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public groups(int id_group, String name, String description) {
        this.id_group = id_group;
        this.name = name;
        this.description = description;
    }
}
