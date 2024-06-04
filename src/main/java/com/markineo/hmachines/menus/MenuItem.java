package com.markineo.hmachines.menus;

public class MenuItem {
	private int position;
	private String key;
	private String type;
	private String leftAction = null;
	private String rightAction = null;
	private double price = 0;
	
	public MenuItem(int position, String type, String key) {
		this.position = position;
		this.type = type;
		this.key = key;
	}
	
	public MenuItem(int position, String type, String key, String leftAction, String rightAction) {
		this.position = position;
		this.type = type;
		this.key = key;
		this.leftAction = leftAction;
		this.rightAction = rightAction;
	}
	
	
	public MenuItem(int position, String type, String key, String leftAction, String rightAction, double price) {
		this.position = position;
		this.type = type;
		this.key = key;
		this.leftAction = leftAction;
		this.rightAction = rightAction;
		this.price = price;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getType() {
		return type;
	}
	
	public String getLeftAction() {
		return leftAction;
	}
	
	public void setLeftAction(String leftAction) {
		this.leftAction = leftAction;
	}
	
	public String getRightAction() {
		return rightAction;
	}
}
