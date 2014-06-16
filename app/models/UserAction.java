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
public class UserAction extends Model {

	public static final int ACTION_APP_OPEN = 1;
	public static final int ACTION_SORT_FOOD = 2;
	public static final int ACTION_USER_LIST_OPEN = 3;
	public static final int ACTION_WASTE_LIST_OPEN = 4;
	public static final int ACTION_SURVEY_OPEN = 5;
	public static final int ACTION_PROFILE_OPEN = 6;
	
	@Required
	@ManyToOne
    public User user;
	
	public int actionType;
	public String actionData;
	public Date actionDate;
	
	public UserAction(User user, int type, String data, Date date) {
		this.actionType = type;
		this.actionData = data;
		this.actionDate = date;
		this.user = user;
	}
	
}
