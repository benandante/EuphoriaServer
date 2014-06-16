/**
 * 
 */
package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * @author F.Yalvac
 *
 */
@Entity
public class Usage extends Model {

	public static String[] relationTypes = {"Relationship", "Family", "Friends"};
	
	@ManyToOne
	@JoinColumn(name="purchaseID")
	public Purchase purchase;
	
	public double amount;
	public Date date;
	public int relationType;
	public int howManyPeople;
	
	public Usage( Purchase purchase, double amount, Date date) {
		this.purchase = purchase;
		this.amount= amount; 
		this.date = date;
		this.relationType = 0;
		this.howManyPeople = 0;
	}
	
}
