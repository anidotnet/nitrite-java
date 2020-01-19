package org.dizitart.no2.repository.data;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WithNitriteId implements Mappable {
    @Id
    public NitriteId idField;
    public String name;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument()
            .put("idField", idField)
            .put("name", name);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        idField = document.get("idField", NitriteId.class);
        name = document.get("name", String.class);
    }
}
