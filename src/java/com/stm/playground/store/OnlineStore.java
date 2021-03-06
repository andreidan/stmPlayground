package com.stm.playground.store;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import scala.Option;
import akka.stm.Atomic;
import akka.stm.StmUtils;
import akka.stm.TransactionFactory;
import akka.stm.TransactionFactoryBuilder;
import akka.stm.TransactionalMap;
import akka.util.FiniteDuration;

import com.stm.playground.ProductNotAvailableException;
import com.stm.playground.model.Product;

/**
 * 
 * @author andrei
 * 
 */
public class OnlineStore {

	// using a persistent data structure
	private final TransactionalMap<UUID, Product> productsMap;

	// drop the speculative option which might give you a better performance, but it behaves inconsistent ( IMHO ) - regarding the number of retries
	// or even if it WILL retry or not.
	private final TransactionFactory txFactory = new TransactionFactoryBuilder().setSpeculative(false).setMaxRetries(5).setReadonly(false).setTimeout(new FiniteDuration(60, TimeUnit.SECONDS)).setBlockingAllowed(true).setTrackReads(true).build();

	public OnlineStore() {
		super();

		// empty store created
		productsMap = new TransactionalMap<UUID, Product>();
	}

	public Integer addProduct(final Product product) {
		return new Atomic<Integer>(txFactory) {
			@Override
			public Integer atomically() {

				StmUtils.deferred(new Runnable() {

					public void run() {
						System.out.println(Thread.currentThread().getName() + " - ADDED product : << " + product.name + " >> in stock.");
					}
				});

				StmUtils.compensating(new Runnable() {

					public void run() {
						System.out.println(Thread.currentThread().getName() + " - Unable to add product: << " + product.name + " >> in stock. ");
					}
				});

				// intentionally breaking the "law" of transactions being idempotent, but this way we will better see how/when transactions are
				// started and rollbacked
				System.out.println(Thread.currentThread().getName() + " - ADDING one product : << " + product.name + " >> in stock. ");

				Integer newStockValue = product.getAvailableInStockCount() + 1;

				// want a better collision view ... sleep here for 1 sec :)

				product.setNewStockCountValue(newStockValue);

				if (!productsMap.get(product.id).isDefined()) {
					productsMap.put(product.id, product);
				}

				return newStockValue;
			}
		}.execute();
	}

	/**
	 * Will attempt to remove on of the specified product from the stock.
	 * 
	 * @param productId
	 */
	public Integer buyProduct(final UUID productId) {

		return new Atomic<Integer>(txFactory) {
			@Override
			public Integer atomically() {

				// final Product product;

				Option<Product> option = productsMap.get(productId);

				if (option.isDefined()) {

					final Product product = option.get();

					// transaction successfully commited
					StmUtils.deferred(new Runnable() {

						public void run() {
							String productName = product == null ? "UNKNOWN" : product.name;

							System.out.println("\n\n" + Thread.currentThread().getName() + " - WINNER !   Successfully removed one product: << " + productName + " >> from stock. There are " + product.getAvailableInStockCount()
									+ " still available products in stock. \n");
						}
					});

					// do cleanup actions in case of rollback
					StmUtils.compensating(new Runnable() {

						public void run() {
							String productName = product == null ? "UNKNOWN" : product.name;

							System.out.println(Thread.currentThread().getName() + " - UNABLE TO REMOVE PRODUCT: << " + productName + " >> from stock. ");
						}
					});

					// intentionally breaking the "law" of transactions being idempotent, but this way we will better see how/when transactions are
					// started and rollbacked
					System.out.println(Thread.currentThread().getName() + " - Removing one product : << " + product.name + " >> from stock. ");

					Integer nowAvailableInStock = product.getAvailableInStockCount();

					if (nowAvailableInStock > 0) {
						product.setNewStockCountValue(--nowAvailableInStock);
						return nowAvailableInStock;
					} else {
						throw new ProductNotAvailableException("Unable to remove product : << " + product.name + " >> from stock because there aren't any available anymore. ");
					}
				} else {
					throw new ProductNotAvailableException("The product you requested is not available in our store. ");
				}
			}

		}.execute();
	}

	/**
	 * Will return true if we'll find the product in store, and the stock count will be higher than 0. 
	 * 
	 * First retry of the transaction will occur if the product is not in our store, and after this is fulfilled we will retry if we found the product 
	 * in store, but there is none currently available ( current stock count is 0 ).
	 * 
	 * See txFactory configuration - the timout is configured there.
	 * 
	 * @param productId
	 * @return true if the product is available for buy.
	 */
	public boolean waitUntilProductIsAvailable(final UUID productId) {

		return new Atomic<Boolean>(txFactory) {
			@Override
			public Boolean atomically() {

				Option<Product> option = productsMap.get(productId);

				StmUtils.deferred(new Runnable() {

					public void run() {
						System.out.println(Thread.currentThread().getName() + " - SUCCESS, the product with id: "+ productId+" is in stock. ");
					}
				});

				StmUtils.compensating(new Runnable() {

					public void run() {
						System.out.println(Thread.currentThread().getName() + " - FAIL, the product with id: " +productId+ " is not in the store. ");
					}
				});

				if (!option.isDefined()) {
					System.out.println(" RETRYING since we don't have the option in our store right now ! ");
					StmUtils.retry();
				}

				final Product product = option.get();

				if (product.getAvailableInStockCount() == 0) {
					System.out.println(" RETRYING since the stock count for product : " + product.name + " is 0.");
					StmUtils.retry();
				}

				return true;
			}
		}.execute();
	}

}
