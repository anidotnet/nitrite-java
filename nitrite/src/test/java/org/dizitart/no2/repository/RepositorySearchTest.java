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

package org.dizitart.no2.repository;

import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.data.*;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.$;
import static org.dizitart.no2.filters.FluentFilter.when;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class RepositorySearchTest extends BaseObjectRepositoryTest {
    @Test
    public void testFindWithOptions() {
        Cursor<Employee> cursor = employeeRepository.find().limit(0, 1);
        assertEquals(cursor.size(), 1);
        assertNotNull(cursor.firstOrNull());
    }

    @Test
    public void testEmployeeProjection() {
        List<Employee> employeeList = employeeRepository.find().toList();
        List<SubEmployee> subEmployeeList
            = employeeRepository.find().project(SubEmployee.class).toList();

        assertNotNull(employeeList);
        assertNotNull(subEmployeeList);

        assertTrue(employeeList.size() > 0);
        assertTrue(subEmployeeList.size() > 0);

        assertEquals(employeeList.size(), subEmployeeList.size());

        for (int i = 0; i < subEmployeeList.size(); i++) {
            Employee employee = employeeList.get(i);
            SubEmployee subEmployee = subEmployeeList.get(i);

            assertEquals(employee.getEmpId(), subEmployee.getEmpId());
            assertEquals(employee.getJoinDate(), subEmployee.getJoinDate());
            assertEquals(employee.getAddress(), subEmployee.getAddress());
        }

        Cursor<Employee> cursor = employeeRepository.find();
        assertNotNull(cursor.firstOrNull());
        assertNotNull(cursor.toString());
        assertEquals(cursor.toList().size(), employeeList.size());
        assertNotNull(cursor.firstOrNull());
        assertEquals(cursor.toList().size(), employeeList.size());
    }

    @Test
    public void testEmptyResultProjection() {
        employeeRepository.remove(ALL);
        assertNull(employeeRepository.find().firstOrNull());

        assertNull(employeeRepository.find(when("empId").eq(-1))
            .firstOrNull());
    }

    @Test
    public void testGetById() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1L);
        e2.setEmpId(2L);
        e3.setEmpId(3L);
        e4.setEmpId(4L);

        empRepo.insert(e1, e2, e3, e4);

        Employee byId = empRepo.getById(2L);
        assertEquals(byId, e2);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByIdNoId() {
        ObjectRepository<Note> repository = db.getRepository(Note.class);
        Note n1 = DataGenerator.randomNote();
        Note n2 = DataGenerator.randomNote();
        Note n3 = DataGenerator.randomNote();

        assert n1 != null;
        n1.setNoteId(1L);
        assert n2 != null;
        n2.setNoteId(2L);
        assert n3 != null;
        n3.setNoteId(3L);

        repository.insert(n1, n2, n3);

        repository.getById(2L);
    }

    @Test(expected = ValidationException.class)
    public void testGetByIdNullId() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1L);
        e2.setEmpId(2L);
        e3.setEmpId(3L);
        e4.setEmpId(4L);

        empRepo.insert(e1, e2, e3, e4);

        empRepo.getById(null);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByIdWrongType() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1L);
        e2.setEmpId(2L);
        e3.setEmpId(3L);
        e4.setEmpId(4L);

        empRepo.insert(e1, e2, e3, e4);

        Employee byId = empRepo.getById("employee");
        assertNull(byId);
    }

    @Test
    public void testEqualFilterById() {
        Employee employee = employeeRepository.find().firstOrNull();
        long empId = employee.getEmpId();
        Employee emp = employeeRepository.find(when("empId").eq(empId))
            .project(Employee.class).firstOrNull();
        assertEquals(employee, emp);
    }

    @Test
    public void testEqualFilter() {
        Employee employee = employeeRepository.find()
            .firstOrNull();

        Employee emp = employeeRepository.find(when("joinDate").eq(employee.getJoinDate()))
            .project(Employee.class)
            .firstOrNull();
        assertEquals(employee, emp);
    }

    @Test
    public void testStringEqualFilter() {
        ObjectRepository<WithPublicField> repository = db.getRepository(WithPublicField.class);

        WithPublicField object = new WithPublicField();
        object.name = "test";
        object.number = 1;
        repository.insert(object);

        object = new WithPublicField();
        object.name = "test";
        object.number = 2;
        repository.insert(object);

        object = new WithPublicField();
        object.name = "another-test";
        object.number = 3;
        repository.insert(object);

        assertEquals(repository.find(when("name").eq("test")).size(), 2);
    }

    @Test
    public void testAndFilter() {
        Employee emp = employeeRepository.find().firstOrNull();

        long id = emp.getEmpId();
        String address = emp.getAddress();
        Date joinDate = emp.getJoinDate();

        Employee employee = employeeRepository.find(
            when("empId").eq(id)
                .and(
                    when("address").regex(address)
                        .and(
                            when("joinDate").eq(joinDate)))).firstOrNull();

        assertEquals(emp, employee);
    }

    @Test
    public void testOrFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(
            when("empId").eq(id)
                .or(
                    when("address").regex("n/a")
                        .or(
                            when("joinDate").eq(null)))).firstOrNull();

        assertEquals(emp, employee);
    }

    @Test
    public void testNotFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(
            when("empId").eq(id).not()).firstOrNull();
        assertNotEquals(emp, employee);
    }

    @Test
    public void testGreaterFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Ascending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").gt(id))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testGreaterEqualFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Ascending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").gte(id))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testLesserThanFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").lt(id))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testLesserEqualFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").lte(id))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testTextFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        String text = emp.getEmployeeNote().getText();

        List<Employee> employeeList = employeeRepository.find(when("employeeNote.text").text(text))
            .toList();

        assertTrue(employeeList.contains(emp));
    }

    @Test
    public void testRegexFilter() {
        ReadableStream<Employee> employees = employeeRepository.find();
        int count = employees.toList().size();

        List<Employee> employeeList = employeeRepository.find(when("employeeNote.text").regex(".*"))
            .toList();

        assertEquals(employeeList.size(), count);
    }

    @Test
    public void testInFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").in(id, id - 1, id - 2))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 3);

        employeeList = employeeRepository.find(when("empId").in(id - 1, id - 2)).toList();
        assertEquals(employeeList.size(), 2);
    }

    @Test
    public void testNotInFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(when("empId").notIn(id, id - 1, id - 2))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 7);

        employeeList = employeeRepository.find(when("empId").notIn(id - 1, id - 2)).toList();
        assertEquals(employeeList.size(), 8);
    }

    @Test
    public void testElemMatchFilter() {
        final ProductScore score1 = new ProductScore("abc", 10);
        final ProductScore score2 = new ProductScore("abc", 8);
        final ProductScore score3 = new ProductScore("abc", 7);
        final ProductScore score4 = new ProductScore("xyz", 5);
        final ProductScore score5 = new ProductScore("xyz", 7);
        final ProductScore score6 = new ProductScore("xyz", 8);

        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        ElemMatch e1 = new ElemMatch() {{
            setId(1);
            setStrArray(new String[]{"a", "b"});
            setProductScores(new ProductScore[]{score1, score4});
        }};
        ElemMatch e2 = new ElemMatch() {{
            setId(2);
            setStrArray(new String[]{"d", "e"});
            setProductScores(new ProductScore[]{score2, score5});
        }};
        ElemMatch e3 = new ElemMatch() {{
            setId(3);
            setStrArray(new String[]{"a", "f"});
            setProductScores(new ProductScore[]{score3, score6});
        }};

        repository.insert(e1, e2, e3);

        List<ElemMatch> elements = repository.find(
            when("productScores").elemMatch(
                when("product").eq("xyz")
                    .and(when("score").gte(8)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").lte(8).not())).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("product").eq("xyz")
                    .or(when("score").gte(8)))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            when("productScores").elemMatch(
                when("product").eq("xyz"))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").gte(10))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").gt(8))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").lt(7))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").lte(7))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").in(7, 8))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(
            when("productScores").elemMatch(
                when("score").notIn(7, 8))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            when("productScores").elemMatch(
                when("product").regex("xyz"))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(when("strArray").elemMatch($.eq("a"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(
            when("strArray").elemMatch(
                $.eq("a")
                    .or($.eq("f")
                        .or($.eq("b"))).not())).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(when("strArray").elemMatch($.gt("e"))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(when("strArray").elemMatch($.gte("e"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(when("strArray").elemMatch($.lte("b"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(when("strArray").elemMatch($.lt("a"))).toList();
        assertEquals(elements.size(), 0);

        elements = repository.find(when("strArray").elemMatch($.in("a", "f"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(when("strArray").elemMatch($.regex("a"))).toList();
        assertEquals(elements.size(), 2);
    }

    @Test
    public void testFilterAll() {
        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        Cursor<ElemMatch> cursor = repository.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 0);

        repository.insert(new ElemMatch());
        cursor = repository.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testEqualsOnTextIndex() {
        PersonEntity p1 = new PersonEntity("jhonny");
        PersonEntity p2 = new PersonEntity("jhonny");
        PersonEntity p3 = new PersonEntity("jhonny");

        ObjectRepository<PersonEntity> repository = db.getRepository(PersonEntity.class);
        repository.insert(p1);
        repository.insert(p2);
        repository.insert(p3);

        List<PersonEntity> sameNamePeople = repository.find(when("name").eq("jhonny")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(when("name").eq("JHONNY")).toList();
        assertEquals(sameNamePeople.size(), 0);

        sameNamePeople = repository.find(when("name").text("jhonny")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(when("name").text("JHONNY")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(when("name").eq("jhon*")).toList();
        assertEquals(sameNamePeople.size(), 0);

        sameNamePeople = repository.find(when("name").text("jhon*")).toList();
        assertEquals(sameNamePeople.size(), 3);
    }

    @Test
    public void testIssue62() {
        PersonEntity p1 = new PersonEntity("abcd");
        p1.setStatus("Married");

        PersonEntity p2 = new PersonEntity("efgh");
        p2.setStatus("Married");

        PersonEntity p3 = new PersonEntity("ijkl");
        p3.setStatus("Un-Married");

        ObjectRepository<PersonEntity> repository = db.getRepository(PersonEntity.class);
        repository.insert(p1);
        repository.insert(p2);
        repository.insert(p3);

        Filter married = when("status").eq("Married");

        assertEquals(repository.find(married).size(), 2);
        assertEquals(repository.find(married).sort("status", SortOrder.Descending).size(), 2);

        assertEquals(repository.find().sort("status", SortOrder.Descending).firstOrNull().getStatus(), "Un-Married");

        assertEquals(repository.find().sort("status", SortOrder.Ascending).size(), 3);
        assertEquals(repository.find().sort("status", SortOrder.Ascending).firstOrNull().getStatus(), "Married");
    }

    @Test
    public void testRepeatableIndexAnnotation() {
        ObjectRepository<RepeatableIndexTest> repo = db.getRepository(RepeatableIndexTest.class);
        RepeatableIndexTest first = new RepeatableIndexTest();
        first.setAge(12);
        first.setFirstName("fName");
        first.setLastName("lName");
        repo.insert(first);

        assertTrue(repo.hasIndex("firstName"));
        assertTrue(repo.hasIndex("age"));
        assertTrue(repo.hasIndex("lastName"));

        assertEquals(repo.find(when("age").eq(12)).firstOrNull(), first);
    }

    @Test
    public void testIdSet() {
        Cursor<Employee> employees = employeeRepository.find().sort("empId", SortOrder.Ascending);
        assertEquals(employees.size(), 10);
    }
}
