package org.dizitart.no2.sync.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private Set<String> added = new HashSet<>();
    private Set<String> removed = new HashSet<>();
}
