package com.stm.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import akka.stm.Ref;

import com.stm.playground.model.Product;
import com.stm.playground.store.OnlineStore;

public class Main {

	public static void main(String... args) {

		ExecutorService executorService = Executors.newFixedThreadPool(6);

		// init Online Store

		final OnlineStore onlineStore = new OnlineStore();
		final Product product = new Product("slash t-shirt ", " cool ! ", new Ref<Long>(200L), new Ref<Integer>(2));

		onlineStore.addProduct(product);

		Callable<Integer> removeProductCallable = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				onlineStore.buyProduct(product.id);
				return null;
			}
		};

		Callable<Integer> addProductsCallable = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				onlineStore.addProduct(product);
				return null;
			}
		};

		List<Future<Integer>> removeProductFutures = new ArrayList<Future<Integer>>();

		for (int i = 0; i < 4; i++) {
			removeProductFutures.add(executorService.submit(removeProductCallable));
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(" \n\n\n\n ------------------- More add/remove operations-------------------- \n\n\n ");

		for (int i = 0; i < 10; i++) {
			if (i % 3 == 0) {
				executorService.submit(removeProductCallable);
			} else {
				executorService.submit(addProductsCallable);
			}
		}
	}
}
