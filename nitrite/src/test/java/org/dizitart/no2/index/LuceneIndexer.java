package org.dizitart.no2.index;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.events.StoreEvents;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 *
 * //TODO: https://howtodoinjava.com/lucene/lucene-index-search-examples/
 * @author Anindya Chatterjee
 */
public class LuceneIndexer implements TextIndexer {
    private static final String CONTENT_ID = "content_id";
    private static final int MAX_SEARCH = Byte.MAX_VALUE;

    private IndexWriter indexWriter;
    private ObjectMapper keySerializer;
    private Analyzer analyzer;
    private Directory indexDirectory;

    public LuceneIndexer() {
        try {
            this.keySerializer = new ObjectMapper();
            keySerializer.setVisibility(keySerializer
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

            indexDirectory = new RAMDirectory();
            analyzer = new StandardAnalyzer();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDirectory, iwc);
        } catch (IOException e) {
            throw new IndexingException("could not create full-text index", e);
        }
    }

    @Override
    public Set<NitriteId> findText(String collectionName, String field, String searchString) {
        IndexReader indexReader = null;
        try {
            QueryParser parser = new QueryParser(field, analyzer);
            parser.setAllowLeadingWildcard(true);
            Query query = parser.parse("*" + searchString + "*");

            indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_SEARCH);
            indexSearcher.search(query, collector);

            TopDocs hits = collector.topDocs(0, MAX_SEARCH);

            Set<NitriteId> keySet = new LinkedHashSet<>();
            if (hits != null) {
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                if (scoreDocs != null) {
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        org.apache.lucene.document.Document document = indexSearcher.doc(scoreDoc.doc);
                        String jsonId = document.get(CONTENT_ID);
                        NitriteId nitriteId = keySerializer.readValue(jsonId, NitriteId.class);
                        keySet.add(nitriteId);
                    }
                }
            }

            return keySet;
        } catch (IOException | ParseException e) {
            throw new IndexingException("could not search on full-text index", e);
        } finally {
            try {
                if (indexReader != null)
                    indexReader.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    @Override
    public String getIndexType() {
        return IndexType.Fulltext;
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        try {
            org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
            String jsonId = keySerializer.writeValueAsString(nitriteId);
            Field contentField = new TextField(field, (String) fieldValue, Field.Store.NO);
            Field idField = new StringField(CONTENT_ID, jsonId, Field.Store.YES);

            document.add(idField);
            document.add(contentField);

            synchronized (this) {
                indexWriter.addDocument(document);
            }
        } catch (IOException ioe) {
            throw new IndexingException("could not write full-text index data for " + fieldValue, ioe);
        }
    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        try {
            String jsonId = keySerializer.writeValueAsString(nitriteId);
            org.apache.lucene.document.Document document = getDocument(jsonId);
            if (document == null) {
                document = new org.apache.lucene.document.Document();
                Field idField = new StringField(CONTENT_ID, jsonId, Field.Store.YES);
                document.add(idField);
            }
            Field contentField = new TextField(field, (String) fieldValue, Field.Store.YES);

            document.add(contentField);

            synchronized (this) {
                indexWriter.updateDocument(new Term(CONTENT_ID, jsonId), document);
            }
        } catch (IOException ioe) {
            throw new IndexingException("could not update full-text index for " + fieldValue, ioe);
        }
    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {
        try {
            String jsonId = keySerializer.writeValueAsString(nitriteId);
            org.apache.lucene.document.Document document = getDocument(jsonId);
            if (document == null) {
                document = new org.apache.lucene.document.Document();
                Field idField = new StringField(CONTENT_ID, jsonId, Field.Store.YES);
                document.add(idField);
            }
            Field contentField = new TextField(field, (String) newValue, Field.Store.YES);

            document.add(contentField);

            synchronized (this) {
                indexWriter.updateDocument(new Term(CONTENT_ID, jsonId), document);
            }
        } catch (IOException ioe) {
            throw new IndexingException("could not update full-text index for " + newValue, ioe);
        }
    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {
        if (!isNullOrEmpty(field)) {
            try {
                Query query;
                QueryParser parser = new QueryParser(field, analyzer);
                parser.setAllowLeadingWildcard(true);
                try {
                    query = parser.parse("*");
                } catch (ParseException e) {
                    throw new IndexingException("could not remove full-text index for value " + field);
                }

                synchronized (this) {
                    indexWriter.deleteDocuments(query);
                }
            } catch (IOException ioe) {
                throw new IndexingException("could not remove full-text index for value " + field);
            }
        }
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        nitriteConfig.getNitriteStore().subscribe(eventInfo -> {
            if (eventInfo.getEvent() == StoreEvents.Commit) {
                this.commit();
            } else if (eventInfo.getEvent() == StoreEvents.Closing) {
                this.close();
            }
        });
    }

    private void commit() {
        try {
            indexWriter.commit();
        } catch (IOException e) {
            throw new IndexingException("could not commit unsaved changes", e);
        }
    }

    private void close() {
        if (indexWriter != null) {
            try {
                commit();
                indexWriter.close();
            } catch (IOException ioe) {
                // ignore it
            }
        }
    }

    private org.apache.lucene.document.Document getDocument(String jsonId) {
        IndexReader indexReader = null;
        try {
            Term idTerm = new Term(CONTENT_ID, jsonId);

            TermQuery query = new TermQuery(idTerm);

            indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_SEARCH);
            indexSearcher.search(query, collector);

            TopDocs hits = collector.topDocs(0, MAX_SEARCH);
            org.apache.lucene.document.Document document = null;
            if (hits != null) {
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                if (scoreDocs != null) {
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        document = indexSearcher.doc(scoreDoc.doc);
                    }
                }
            }

            return document;
        } catch (IOException e) {
            throw new IndexingException("could not search on full-text index", e);
        } finally {
            try {
                if (indexReader != null)
                    indexReader.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }
}
