package com.stm.playground.model;

import java.util.UUID;

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
	private final Integer price;
	private Ref<Integer> availableInStockCount;

	public Product(String name, String description, Integer price) {
		super();
		id = UUID.randomUUID();
		this.name = name;
		this.description = description;
		this.price = price;
		// 0 in stock by default
		this.availableInStockCount = new Ref<Integer>(0);
	}

	public Integer getAvailableInStockCount() {
		return availableInStockCount.getOrWait();
	}

	public void setNewStockCountValue(final Integer newCount) {
		availableInStockCount.swap(newCount);
	}
}
