package com.stm.playground;

/**
 * Exception thrown when trying to remove a product when there aren't any more in stock or when attempting to buy a product which is not available at
 * all in the store.
 * 
 * @author andrei
 * 
 */
public class ProductNotAvailableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2531042279422463509L;

	public ProductNotAvailableException() {
		super();
	}

	public ProductNotAvailableException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ProductNotAvailableException(String arg0) {
		super(arg0);
	}

	public ProductNotAvailableException(Throwable arg0) {
		super(arg0);
	}

}
