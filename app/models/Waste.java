package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Waste  extends Model{
	 
	public static String[] wasteTypes = {"Not selected", "Change of plans", "Did not like it", "It was unhealthy", "It was unhealthy", 
		"No shopping list", "Over-buying", "Special offer", "Use by date passed","Visibility of stock is missing", "Wrong storage", "Other",  "There is no waste"};
	public static final int STATUS_WASTED = 0;
	public static final int STATUS_DELETED = 1;
	
	@ManyToOne
	@JoinColumn(name="purchaseID")
	public Purchase purchase;
	
	public double amount;
	public Date date;
	public int wasteType;
	public Date deletionDate;
	public int deleted;
	
	public Waste(Purchase purchase, double amount, Date date, int wasteType) {
		this.purchase = purchase;
		this.amount= amount; 
		this.date = date;
		this.wasteType = wasteType;
		this.deleted = STATUS_WASTED;
	}
	
}
