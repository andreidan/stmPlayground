package com.stm.playground.store;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import akka.stm.Atomic;

import com.stm.playground.model.Product;

/**
 * 
 * @author andrei
 * 
 */
@SuppressWarnings("unused")
public class OnlineStore {

	private Map<UUID, Product> products;

	public OnlineStore() {
		super();

		// empty store created
		products = new ConcurrentHashMap<UUID, Product>();
	}

	public void addProduct(Product product) {
		products.put(product.id, product);
		product.addProductToStock();
	}

	public void buyProduct(final UUID productId) {
		// wrapping this inside a transaction
		new Atomic() {
			@Override
			public Object atomically() {

				Product product = products.get(productId);

				if (product != null) {
					product.removeProductFromStock();
				}
				return null;
			}
		}.execute();
	}

}
