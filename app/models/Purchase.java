package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Purchase extends Model {

	public static final int STATUS_SHOPPING = 0;
	public static final int STATUS_AVAILABLE = 1;
	public static final int STATUS_DELETED = 2;
	
	@Required
	@ManyToOne
    public User user;
	
	@Required
	@ManyToOne
	public Food food;
	
	public Date date;
	public double amount;
	public double used;
	public double wasted;
	public int status; /* 0: shopping, 1: available, 2: deleted */
	public Date deletionDate;
	public Date statusChangeDate;
	
	public Purchase(User user, Food food, Date date, double amount) {
		this.user = user;
		this.food = food;
		this.date = date;
		this.amount = amount;
		used = 0;
		wasted = 0;
		status = STATUS_SHOPPING;
	}
	
	public double getRemainingAmount() {
		return (amount - used); 
	}
	
}
