package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

@Entity
public class Food extends Model {

	public static String foodCategories[];

	public String name;
	public String category;
	public String unit;
	public String icon;

	public Food(String name, String category, String unit, String icon) {
		this.name = name;
		this.category = category;
		this.unit = unit;
		this.icon = icon;
	}
}


