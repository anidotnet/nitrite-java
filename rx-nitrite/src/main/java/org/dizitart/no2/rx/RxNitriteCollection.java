package org.dizitart.no2.rx;

import io.reactivex.Single;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.filters.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public interface RxNitriteCollection extends RxPersistentCollection<Document> {
    default FlowableWriteResult insert(Document document, Document... documents) {
        notNull(document, "a null document cannot be inserted");
        if (documents != null) {
            containsNull(documents, "a null document cannot be inserted");
        }

        List<Document> documentList = new ArrayList<>();
        documentList.add(document);

        if (documents != null && documents.length > 0) {
            Collections.addAll(documentList, documents);
        }

        return insert(documentList.toArray(new Document[0]));
    }

    default FlowableWriteResult update(Filter filter, Document update) {
        return update(filter, update, new UpdateOptions());
    }

    FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    default FlowableWriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    FlowableWriteResult remove(Filter filter, boolean justOne);

    FlowableDocumentCursor find();

    FlowableDocumentCursor find(Filter filter);

    Single<Document> getById(NitriteId nitriteId);

    String getName();
}
