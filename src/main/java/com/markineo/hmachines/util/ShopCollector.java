package com.markineo.hmachines.util;

public class ShopCollector {
	private String key;
	private String product;
	private int size;
	
	/*
	 * 1 -> selecione a quantidade
	 * 2 -> digite confirmar 
	 */
	private int stage;
	
	/**
	 * 
	 * @param key A key do produto em questão.
	 * @param type O tipo de produto.
	 * @param size Quantidade.
	 * @param stage
	 */
	
	public ShopCollector(String key, String product, int size, int stage) {
		this.key = key;
		this.product = product;
		this.size = size;
		this.stage = stage;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getProduct() {
		return product;
	}
	
	public int getSize() {
		return size;
	}
	
	public void updateSize(int size) {
		this.size = size;
	}
	
	public int getStage() {
		return stage;
	}
	
	public void updateStage(int stage) {
		this.stage = stage;
	}
	
}
