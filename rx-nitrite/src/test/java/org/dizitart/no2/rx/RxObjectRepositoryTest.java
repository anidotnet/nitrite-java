package org.dizitart.no2.rx;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subscribers.TestSubscriber;
import org.dizitart.no2.collection.NitriteId;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RxObjectRepositoryTest extends RxBaseTest {
    private RxObjectRepository<Employee> repository;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        repository = db.getRepository(Employee.class);
    }

    @Test
    public void testInsert() {
        Flowable<NitriteId> flowable = repository.insert(testData.toArray(new Employee[0]));
        assertEquals(0, repository.find().size().blockingGet().intValue());

        // TODO: Resolve flowable issue
        repository.observe().subscribe(System.out::println);
//            .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                @Override
//                public void onNext(ChangedItem<Employee> item) {
//                    System.out.println(item);
//                }
//            });

        assertEquals(0, repository.find().size().blockingGet().intValue());
        TestSubscriber<NitriteId> test = flowable.test();
        test.awaitTerminalEvent();
        test.assertComplete();
        test.assertNoErrors();
        assertEquals(2, repository.find().size().blockingGet().intValue());
    }

//    @Test
//    public void testUpdate() {
//        Employee e1 = new Employee("John Doe", 35);
//        Employee e2 = new Employee("Jane Doe", 30);
//        Flowable<NitriteId> flowable = repository.insert(e1, e2);
//        assertEquals(0, repository.find().size().blockingGet().intValue());
//
//        repository.observe(ChangeType.INSERT, BackpressureStrategy.MISSING)
//            .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                @Override
//                public void onNext(ChangedItem<Employee> item) {
//                    System.out.println(item);
//                }
//            });
//
//        assertEquals(0, repository.find().size().blockingGet().intValue());
//        TestSubscriber<NitriteId> test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(2, repository.find().size().blockingGet().intValue());
//
//        flowable = repository.update(eq("name", "John Doe"),
//            new Employee("RxNitrite", 36));
//        repository.observe(ChangeType.UPDATE, BackpressureStrategy.MISSING)
//            .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                @Override
//                public void onNext(ChangedItem<Employee> item) {
//                    System.out.println(item);
//                }
//            });
//
//        assertEquals(2, repository.find().size().blockingGet().intValue());
//        assertEquals(repository.find(eq("name", "RxNitrite")).totalCount().blockingGet().intValue(), 0);
//        test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(repository.find(eq("name", "RxNitrite")).totalCount().blockingGet().intValue(), 1);
//        assertEquals(2, repository.find().size().blockingGet().intValue());
//
//        flowable = repository.update(new Employee("RxNitrite", 40));
//        assertEquals(repository.find(eq("name", "RxNitrite")).firstOrNull().blockingGet().getAge().longValue(), 36L);
//        test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(repository.find(eq("name", "RxNitrite")).firstOrNull().blockingGet().getAge().longValue(), 40L);
//
//        Employee employee = new Employee();
//        employee.setName("Iron Man");
//        employee.setAge(99);
//        flowable = repository.update(eq("name", "RxNitrite"), employee, true);
//        assertNull(repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet());
//        test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(99, repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet().getAge().longValue());
//
//        flowable = repository.update(eq("name", "Iron Man"), Document.createDocument("age", 100));
//        assertEquals(99, repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet().getAge().longValue());
//        test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(100, repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet().getAge().longValue());
//
//        flowable = repository.update(eq("name", "Iron Man"), Document.createDocument("age", 200), true);
//        assertEquals(100, repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet().getAge().longValue());
//        test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(200, repository.find(eq("name", "Iron Man")).firstOrNull().blockingGet().getAge().longValue());
//    }
//
//    @Test
//    public void testUpsert() throws InterruptedException {
//        AtomicInteger insertCount = new AtomicInteger(0);
//        AtomicInteger updateCount = new AtomicInteger(0);
//
//        repository.observe(ChangeType.INSERT, BackpressureStrategy.MISSING)
//                .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                    @Override
//                    public void onNext(ChangedItem<Employee> item) {
//                        insertCount.incrementAndGet();
//                    }
//                });
//
//        repository.observe(ChangeType.UPDATE, BackpressureStrategy.MISSING)
//            .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                @Override
//                public void onNext(ChangedItem<Employee> item) {
//                    updateCount.incrementAndGet();
//                }
//            });
//
//
//        assertEquals(repository.find().size().blockingGet().intValue(), 0);
//
//        final PodamFactory factory = new PodamFactoryImpl();
//        ExecutorService pool = Executors.newCachedThreadPool();
//        final CountDownLatch latch = new CountDownLatch(10000);
//        String[] names = new String[] {"Iron Man", "Captain America", "Thor", "Hulk", "Black Widow", "Black Panther"};
//
//        AtomicInteger count = new AtomicInteger(0);
//        for (int i = 0; i < 10000; i++) {
//            pool.submit(() -> {
//                int index = count.getAndIncrement() % 6;
//                Employee employee = factory.manufacturePojoWithFullData(Employee.class);
//                employee.setName(names[index]);
//                TestSubscriber<NitriteId> testSubscriber = repository.update(employee, true).test();
//                testSubscriber.awaitTerminalEvent();
//                testSubscriber.assertComplete();
//                testSubscriber.assertNoErrors();
//                latch.countDown();
//            });
//        }
//
//        latch.await();
//
//        assertEquals(6, repository.find().size().blockingGet().intValue());
//        assertTrue(insertCount.get() <= 6);
//        assertTrue(updateCount.get() <= 10000 - insertCount.get());
//    }
//
//    @Test
//    public void testRemove() {
//        Flowable<NitriteId> flowable = repository.insert(testData.toArray(new Employee[0]));
//        assertEquals(0, repository.find().size().blockingGet().intValue());
//        TestSubscriber<NitriteId> test = flowable.test();
//        test.awaitTerminalEvent();
//        test.assertComplete();
//        test.assertNoErrors();
//        assertEquals(2, repository.find().size().blockingGet().intValue());
//
//        repository.observe(BackpressureStrategy.MISSING)
//            .subscribe(new BaseSubscriber<ChangedItem<Employee>>() {
//                @Override
//                public void onNext(ChangedItem<Employee> item) {
//                    System.out.println(item.getItem() + " has been " + item.getChangeType() + "ED");
//                }
//            });
//    }

    @Test
    public void test() throws InterruptedException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        ObservableOnSubscribe<String> handler = emitter -> {
            executor.schedule(() -> {
                emitter.onNext(String.valueOf(System.currentTimeMillis()));
                emitter.onComplete();
            }, 500, TimeUnit.MILLISECONDS);
        };

        Observable<String> observable = Observable.create(handler);

        observable.subscribe(System.out::println, Throwable::printStackTrace,
            () -> System.out.println("Done"));

        Thread.sleep(50000);
        executor.shutdown();
    }
}
