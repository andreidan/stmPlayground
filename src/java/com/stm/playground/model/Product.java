package com.stm.playground.model;

import java.util.UUID;

import akka.stm.Atomic;
import akka.stm.Ref;

/**
 * 
 * @author andrei
 * 
 */
@SuppressWarnings("unused")
public class Product {

	public final UUID id;
	public final String name;
	public final String description;
	private Ref<Long> price;
	private Ref<Integer> availableInStockCount;

	public Product(String name, String description, Ref<Long> price, Ref<Integer> availableInStockCount) {
		super();
		id = UUID.randomUUID();
		this.name = name;
		this.description = description;
		this.price = price;
		this.availableInStockCount = availableInStockCount;
	}

	public Integer getAvailableInStockCount() {
		return availableInStockCount.getOrWait();
	}

	public void addProductToStock() {
		new Atomic() {
			@Override
			public Object atomically() {
				System.out.println(Thread.currentThread().getName() + " - Product : " + name + ". Now in stock: " + availableInStockCount.getOrWait());
				availableInStockCount.swap(availableInStockCount.getOrWait() + 1);
				System.out.println(Thread.currentThread().getName() + " - Added product " + name + " to stock. Current stock: " + availableInStockCount.getOrWait());

				return null;
			}
		}.execute();
	}

	public void removeProductFromStock() {
		new Atomic() {
			@Override
			public Object atomically() {

				System.out.println(Thread.currentThread().getName() + " - Removing one  product : " + name + "  from  stock. ");

				if (getAvailableInStockCount() > 0) {
					System.out.println(Thread.currentThread().getName() + " -  We now have " + availableInStockCount.getOrAwait() + " " + name + " products in stock. ");
					availableInStockCount.swap(getAvailableInStockCount() - 1);

					System.out.println(Thread.currentThread().getName() + " -  Removed one product " + name + " from stock. There are " + getAvailableInStockCount() + " still available products in stock. ");
				} else {
					System.out.println(Thread.currentThread().getName() + " -  NO MORE PRODUCTS IN STOCK ! ");
				}
				return null;
			}
		}.execute();
	}

}
