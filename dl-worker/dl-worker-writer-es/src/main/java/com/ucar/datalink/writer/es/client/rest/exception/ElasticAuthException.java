package com.ucar.datalink.writer.es.client.rest.exception;

public class ElasticAuthException extends RuntimeException {


	private static final long serialVersionUID = -5781507163935151142L;

	public ElasticAuthException(String message){
		super(message);
	}

	public ElasticAuthException(String message, Throwable e){
		super(message,e);
	}
}
