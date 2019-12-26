/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.spatial;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public class SpatialIndexer implements Indexer {
    private IndexCatalog indexCatalog;
    private NitriteStore nitriteStore;

    public Set<NitriteId> findEqual(String collectionName, String field, Geometry geometry, EqualityType equalityType) {
        Set<NitriteId> resultSet = new LinkedHashSet<>();
        NitriteRTree<?, Geometry> indexMap = getIndexMap(collectionName, field);
        for (KeyValuePair<?, Geometry> entry : indexMap.entries()) {
            Geometry geom = entry.getValue();
            SpatialKey key = entry.getKey();

        }
    }

    public Set<NitriteId> findWithin(String collectionName, String field, Geometry geometry){

    }

    public Set<NitriteId> findIntersects(String collectionName, String field, Geometry geometry) {

    }

    @Override
    public String getIndexType() {
        return "Spatial";
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {

    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {

    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {

    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = this.nitriteStore.getIndexCatalog();
    }

    @SuppressWarnings("rawtypes")
    private NitriteRTree<BoundingBox, ConcurrentSkipListSet<NitriteId>> getIndexMap(String collectionName, String field) {
        String mapName = getIndexMapName(collectionName, field);
        return nitriteStore.openRTree(mapName);
    }
}
