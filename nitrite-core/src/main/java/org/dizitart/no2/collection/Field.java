package org.dizitart.no2.collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
@EqualsAndHashCode
public class Field implements Serializable {
    private static final long serialVersionUID = 1574320176L;

    @Getter
    private String name;

    private Field(String name) {
        this.name = name;
    }

    public static Field of(String name) {
        return new Field(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
