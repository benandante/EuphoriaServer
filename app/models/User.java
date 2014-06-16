package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
 
@Entity
public class User extends GenericModel {
 
	 @Id
	public String username;
	 
    public String email;
    public String password;
    public int age;
    public int gender; /*0:gender, 1: female, 2:male */
    public String address;
    
    
    public User(String username, String email, String password) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
 
    
}