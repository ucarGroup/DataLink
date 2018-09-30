package com.ucar.datalink.writer.es.client.rest.exception;

public class ElasticSearchException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5912482647742431672L;

	public ElasticSearchException(String message){
		super(message);
	}
	
	public ElasticSearchException(String message, Throwable e){
		super(message,e);
	}
}
