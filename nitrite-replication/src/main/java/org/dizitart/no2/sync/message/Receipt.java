package org.dizitart.no2.sync.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class Receipt {
    private Set<Long> added;
    private Set<Long> removed;
}
