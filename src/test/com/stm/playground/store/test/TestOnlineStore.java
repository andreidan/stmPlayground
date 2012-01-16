package com.stm.playground.store.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.multiverse.api.exceptions.RetryTimeoutException;

import com.stm.playground.model.Product;
import com.stm.playground.store.OnlineStore;

/**
 * 
 * @author andrei
 *
 */
public class TestOnlineStore {

	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	@Rule
	public TestName runningTestName = new TestName();

	@Before
	public void setUp() {
		System.out.println(" \n\n\n  ----------================ Setting up TEST: " + runningTestName.getMethodName() + "! ================----------\n\n\n");
	}

	/**
	 * Test to simulate 6 threads trying to buy the same product, when only 3 products are available in stock.
	 */
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

	/**
	 * Test to simulate buy actions while add actions are being performed.
	 */
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

	/**
	 * Test attempt to wait until the product is brought to store and until product stock count is higher than 0.
	 */
	@Test
	public void testWaitUntilProductIsAvailable() {
		final OnlineStore onlineStore = new OnlineStore();
		final Product product = new Product("Hyundai", "i30 - rocks ! ", 1000);

		// waiting until a product is brought into the store
		
		executorService.submit(new Runnable() {

			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// ouch
				}

				onlineStore.addProduct(product);
			}
		});

		boolean isProductInStock = false;
		try {
			isProductInStock = onlineStore.waitUntilProductIsAvailable(product.id);
		} catch (RetryTimeoutException e) {
			fail("Didn't expected a RetryTimeoutException. ");
		}
		
		assertTrue("Product: "+product.name+" should have arrived in stock. ", isProductInStock);

		// reset stock count. At this point, the product was in store, but there is no item currently available.
		product.setNewStockCountValue(0);
		
		System.out.println("\n\n  EMPTIED stock for product: "+product.name +" . \n\n");
		
		for (int i = 0; i < 4; i++) {
			executorService.submit(new Runnable() {

				public void run() {
					try {
						// sleeping more than 60 seconds, in order to get a timeout exception
						Thread.sleep(62000);
					} catch (InterruptedException e) {
						// ignore
					}

					onlineStore.addProduct(product);
				}
			});
		}

		try {
			onlineStore.waitUntilProductIsAvailable(product.id);
			fail("We were expecting a RetryTimeoutException. ");
		} catch (RetryTimeoutException e1) {
			; // this is what we are expecting
		}

	}

}
