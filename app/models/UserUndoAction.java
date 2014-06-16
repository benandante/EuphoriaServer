/**
 * 
 */
package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author F.Yalvac
 *
 */
@Entity
public class UserUndoAction extends Model {
	
	public static int UNDO_AMOUNT_UPDATE = 1;
	public static int UNDO_CONSUMPTION = 2;
	
	@Required
	@ManyToOne
    public User user;
	
	
	@ManyToOne
	public Purchase purchase;
	
	public int type;
	
	public UserUndoAction(User user, int type) {
		this.user = user;
		this.type = type;
	}
}
