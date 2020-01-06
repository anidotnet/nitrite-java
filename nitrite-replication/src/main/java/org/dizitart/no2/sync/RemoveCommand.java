package org.dizitart.no2.sync;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class RemoveCommand<E> implements Command<E> {
    private String crdtId;
    private Set<Element<E>> elements;
}
