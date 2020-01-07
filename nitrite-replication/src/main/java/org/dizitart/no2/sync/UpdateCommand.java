package org.dizitart.no2.sync;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class UpdateCommand<E> implements Command<E> {
    private String crdtId;
    private Element<E> element;
}