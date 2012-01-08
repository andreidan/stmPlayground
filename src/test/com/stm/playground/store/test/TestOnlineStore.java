package com.stm.playground.store.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.stm.playground.model.Product;
import com.stm.playground.store.OnlineStore;

public class TestOnlineStore {

	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	@Rule
	public TestName runningTestName = new TestName();

	@Before
	public void setUp() {
		System.out.println(" \n Setting up TEST: " + runningTestName.getMethodName() + "! \n");
	}

	@Test
	public void testBuyProduct() {
		final OnlineStore onlineStore = new OnlineStore();
		final Product product = new Product("Slash t-shirt ", "cool duo guitar shirt. ", 200);

		// expecting 3 SUCCESSFUL removals. When those are accomplished we should have 0 products in stock.
		final CountDownLatch countDownLatch = new CountDownLatch(3);

		onlineStore.addProduct(product);
		onlineStore.addProduct(product);
		onlineStore.addProduct(product);

		for (int i = 0; i < 6; i++) {
			executorService.submit(new Runnable() {

				public void run() {
					onlineStore.buyProduct(product.id);

					// buy successful ... counting down
					countDownLatch.countDown();
				}
			});
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// oh well ...
		}

		// the product should have 0 available stock count
		assertEquals("The online store should not have any product: " + product.name + " in stock. ", (Integer) 0, (Integer) product.getAvailableInStockCount());
	}

	@Test
	public void testAddRemoveProducts() {
		final OnlineStore onlineStore = new OnlineStore();
		final Product product = new Product("Top hat", "cool hat! ", 400);

		final CountDownLatch countDownLatch = new CountDownLatch(9);

		// add product to stock
		for (int i = 0; i < 7; i++) {
			executorService.submit(new Runnable() {

				public void run() {
					onlineStore.addProduct(product);

					// buy successful ... counting down
					countDownLatch.countDown();
				}
			});
		}

		// wait until there is at least one product in the store
		while (countDownLatch.getCount() == 7) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// yeah
			}
		}

		// knowing that we now have at least one product in the store, try to buy 2
		for (int i = 0; i < 2; i++) {
			executorService.submit(new Runnable() {

				public void run() {
					onlineStore.buyProduct(product.id);

					// buy successful ... counting down
					countDownLatch.countDown();
				}

			});
		}

		try {
			// wait until all threads are done adding/buying products
			countDownLatch.await();
		} catch (InterruptedException e) {
			// oh well ...
		}

		// the product should have 5 available stock count
		assertEquals((Integer) 5, (Integer) product.getAvailableInStockCount());
	}

}
