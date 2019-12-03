package org.dizitart.no2.repository;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.PersistentCollection;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.filters.Filter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a type-safe persistent java object collection. An object repository
 * is backed by a {@link org.dizitart.no2.collection.NitriteCollection}, where all objects are converted
 * into a {@link Document} and saved into the database.
 *
 * An object repository is observable like its underlying {@link org.dizitart.no2.collection.NitriteCollection}.
 *
 * [[app-listing]]
 * [source,java]
 * .Create a repository
 * --
 * // create/open a database
 *  Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 * // create an object repository
 * ObjectRepository<Employee> employeeStore = db.getRepository(Employee.class);
 *
 * // observe any change to the repository
 * employeeStore.register(new ChangeListener() {
 *
 *      @Override
 *      public void onChange(ChangeInfo changeInfo) {
 *          // your logic based on action
 *      }
 *  });
 *
 * // insert an object
 * Employee emp = new Employee();
 * emp.setName("John Doe");
 * employeeStore.insert(emp);
 *
 * --
 *
 * @param <T> the type of the object to store.
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see org.dizitart.no2.collection.events.ChangeAware
 * @see Document
 * @see org.dizitart.no2.NitriteId
 * @see org.dizitart.no2.collection.events.ChangeListener
 * @see org.dizitart.no2.common.event.EventBus
 * @see org.dizitart.no2.collection.NitriteCollection
 */
public interface ObjectRepository<T> extends PersistentCollection<T> {
    /**
     * Inserts objects into this repository. If the object contains a value marked with
     * {@link org.dizitart.no2.index.annotations.Id}, then the value will be used as an unique key to identify the object
     * in the repository. If the object does not contain any value marked with {@link org.dizitart.no2.index.annotations.Id},
     * then nitrite will generate a new {@link org.dizitart.no2.NitriteId} and will add it to the document
     * generated from the object.
     *
     * If any of the value is already indexed in the repository, then after insertion the
     * index will also be updated.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param object    the object to insert
     * @param others    other objects to insert in a batch.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if `object` is `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the id value contains `null` value.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the id value contains non comparable type, i.e.
     * type that does not implement {@link Comparable}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the id contains value which is not of the same java
     * type as of other objects' id in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if the value of id value clashes with the id
     * of another object in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if a value of the object is indexed and it
     * violates the unique constraint in the collection(if any).
     * @see org.dizitart.no2.NitriteId
     * @see WriteResult
     */
    @SuppressWarnings("unchecked")
    default WriteResult insert(T object, T... others) {
        notNull(object, "a null object cannot be inserted");
        if (others != null) {
            containsNull(others, "a null object cannot be inserted");
        }

        List<T> itemList = new ArrayList<>();
        itemList.add(object);

        if (others != null && itemList.size() > 0) {
            Collections.addAll(itemList, others);
        }

        return insert(itemList.toArray((T[]) Array.newInstance(object.getClass(), 0)));
    }

    /**
     * Updates objects in the repository. If the filter does not find
     * any object in the collection, then the `update` object will be inserted.
     *
     * If the `filter` is `null`, it will update all objects in the collection.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    default WriteResult update(Filter filter, T update) {
        return update(filter, update, false);
    }

    /**
     * Updates objects in the repository. Update operation can be customized
     * with the help of `updateOptions`.
     *
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true` in `updateOptions`.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * If the `update` object has a non `null` value in the id value, this value
     * will be removed before update.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update} or
     * {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param filter        the filter to apply to select objects from the collection.
     * @param update        the modifications to apply.
     * @param upsert        if set to `true`, `update` object will be inserted if not found.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     * @throws org.dizitart.no2.exceptions.ValidationException if `updateOptions` is `null`.
     */
    WriteResult update(Filter filter, T update, boolean upsert);

    /**
     * Updates objects in the repository by setting the field specified in `document`.
     *
     * If the `filter` is `null`, it will update all objects in the collection.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update}.
     *
     * @param filter the filter to apply to select objects from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    default WriteResult update(Filter filter, Document update) {
        return update(filter, update, false);
    }

    /**
     * Updates objects in the repository by setting the field specified in `document`.
     * Update operation can either update the first matching object or all matching
     * objects depending on the value of `justOnce`.
     *
     * If the `filter` is `null`, it will update all objects in the collection unless
     * `justOnce` is set to `true`.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * ====
     * The `update` document should not contain `_id` field.
     * ====
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update}.
     *
     * @param filter        the filter to apply to select objects from the collection.
     * @param update        the modifications to apply.
     * @param justOnce      indicates if update should be applied on first matching object or all.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` object is `null`.
     */
    WriteResult update(Filter filter, Document update, boolean justOnce);

    /**
     * Removes matching elements from the collection.
     *
     * If the `filter` is `null`, it will remove all objects from the collection.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Remove}.
     *
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    default WriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    /**
     * Removes objects from the collection. Remove operation can be customized by
     * `removeOptions`.
     *
     * If the `filter` is `null`, it will remove all objects in the collection unless
     * `justOnce` is set to `true` in `removeOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Remove}.
     *
     * @param filter the filter to apply to select objects from collection.
     * @param justOne indicates if only one element will be removed or all of them.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter, boolean justOne);

    /**
     * Returns a cursor to all objects in the collection.
     *
     * @return a cursor to all objects in the collection.
     */
    Cursor<T> find();

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected objects.
     *
     * See {@link Filter} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter the filter to apply to select objects from collection.
     * @return a cursor to all selected objects.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @see Filter
     * @see Cursor#project(Class)
     */
    Cursor<T> find(Filter filter);

    /**
     * Returns the type associated with the {@link ObjectRepository}.
     *
     * @return type of the object.
     * */
    Class<T> getType();

    /**
     * Returns the underlying document collection.
     *
     * @return the underlying document collection.
     * */
    NitriteCollection getDocumentCollection();
}
