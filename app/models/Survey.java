/**
 * 
 */
package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author F.Yalvac
 *
 */
@Entity
public class Survey extends Model {
	
	@Required
	@ManyToOne
    public User user;
	
	public int Q1;
	public int Q2;
	public int Q3;
	public int Q4;
	
	public Date date;
	
	public Survey(User user, int q1, int q2, int q3, int q4, Date date) {
		this.user = user;
		this.Q1 = q1;
		this.Q2 = q2;
		this.Q3 = q3;
		this.Q4 = q4;
		this.date = date;
	}
}


