/**
 * 
 */
package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

import models.Food;
import models.Purchase;
import models.Survey;
import models.Usage;
import models.User;
import models.UserAction;
import models.Waste;

/**
 * @author F.Yalvac
 * 
 */

@OnApplicationStart
public class DatabaseLoader extends Job{

	private static String parentFileName = "";
	
	private static String userFileName = "users.csv";
	private static File userFile = new File(parentFileName, userFileName);
	
	private static String purchaseFileName = "purchase.csv";
	private static File purchaseFile = new File(parentFileName, purchaseFileName);
	
	private static String usageFileName = "usage.csv";
	private static File usageFile = new File(parentFileName, usageFileName);
	
	private static String wasteFileName = "waste.csv";
	private static File wasteFile = new File(parentFileName, wasteFileName);
	
	private static String surveyFileName = "survey.csv";
	private static File surveyFile = new File(parentFileName, surveyFileName);
	
	private static String actionFileName = "action.csv";
	private static File actionFile = new File(parentFileName, actionFileName);
	
	@Override
	public void doJob() throws Exception {
		super.doJob();
		String osname = System.getProperty("os.name");
		if(osname.toLowerCase().contains("windows")) {
			parentFileName = "home" + File.separator + "fulya" + File.separator + "FoodDatabase";
		} else {
			parentFileName = File.separator + "home" + File.separator + "fulya" + File.separator + "FoodDatabase";
		}
		
		File file = new File(parentFileName);
		if(!file.exists()) {
			file.createNewFile();
		}
	//	resetDatabase();
	/*	saveUsersToFile();
		savePurchaseDataToFile();
		saveUsageDataToFile();
		saveWasteDataToFile();
		saveSurveyDataToFile();
		saveUserActionDataToFile();*/
		if(Food.count() == 0) {
			addIngredients();
		}
		if(User.count() == 0) {
			addDefaultUsers();
		}
	/*	
		if(Purchase.count() == 0) {
			addTestPurchaseData();
		}*/
		
		
	}


	private void resetDatabase() {
		Fixtures.deleteAllModels();
		Fixtures.deleteDatabase();
	}
	
	
	private void addTestPurchaseData() {
		User user = User.findById("a");
		Food food = (Food) Food.findAll().get(0);
		Date date = new Date();
		
		Purchase purchase = new Purchase(user, food, date, 10);
		purchase.save();
		
	}


	public void addDefaultUsers() {
		User user = new User("a", "foktay@gmail.com", "b");
		user.save();
	}

	public void addIngredients() {
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(new File(".\\FoodDatabase\\ingredients.csv"));
			DataInputStream in = new DataInputStream(fileInputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			int foodID = 0;
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				String[] values = strLine.split(",");
				if(values != null && values.length >= 4) {
					Food food = new Food(values[0], values[1], values[2], "img/" + values[3]);
					
					foodID++;
					food.save();
				}
			}
			br.close();
			in.close();
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveUsersToFile() {
		List<User> users = User.findAll();
		if(userFile.exists()) {
			userFile.delete();
		}
		try {
			userFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(userFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (User user : users) {
						bufferedWriter.write(user.username + "," + user.gender + "," + user.age + "," + user.address + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "User file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "User write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "User file couln't be created: " + e.getMessage());
		}		
	}
	
	
	public static void savePurchaseDataToFile() {
		List<Purchase> prList = Purchase.findAll();
		if(purchaseFile.exists()) {
			purchaseFile.delete();
		}
		try {
			purchaseFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(purchaseFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (Purchase pr : prList) {
						bufferedWriter.write(pr.user.username + "," + 
								pr.food.name + "," + 
								pr.date.toString() + "," + 
								pr.amount + "," + 
								pr.used + "," + 
								pr.status + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Purchase file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Purchase write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "Purchase file couln't be created: " + e.getMessage());
		}		
	}
	
	public static void saveUsageDataToFile() {
		List<Usage> uList = Usage.findAll();
		if(usageFile.exists()) {
			usageFile.delete();
		}
		try {
			usageFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(usageFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (Usage u : uList) {
						bufferedWriter.write(u.purchase.user.username + "," + 
								u.purchase.food.name + "," + 
								u.purchase.date.toString() + "," +
								u.date.toString() + "," + 
								u.amount + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Usage file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Usage write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "Usage file couln't be created: " + e.getMessage());
		}		
	}
	
	public static void saveWasteDataToFile() {
		List<Waste> wList = Waste.findAll();
		if(wasteFile.exists()) {
			wasteFile.delete();
		}
		try {
			wasteFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(wasteFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (Waste w : wList) {
						bufferedWriter.write(w.purchase.user.username + "," + 
								w.purchase.food.name + "," + 
								w.purchase.date.toString() + "," +
								w.date.toString() + "," + 
								w.wasteType + "," +
								w.amount + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Waste file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Waste write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "Waste file couln't be created: " + e.getMessage());
		}		
	}
	
	public static void saveSurveyDataToFile() {
		List<Survey> sList = Survey.findAll();
		if(surveyFile.exists()) {
			surveyFile.delete();
		}
		try {
			surveyFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(surveyFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (Survey s : sList) {
						bufferedWriter.write(s.user.username + "," + 
								s.Q1 + "," + 
								s.Q2 + "," +
								s.Q3 + "," + 
								s.Q4 + "," +
								s.date.toString() + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Survey file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Survey write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "Survey file couln't be created: " + e.getMessage());
		}		
	}
	
	
	public static void saveUserActionDataToFile() {
		List<UserAction> aList = UserAction.findAll();
		if(actionFile.exists()) {
			actionFile.delete();
		}
		try {
			actionFile.createNewFile();
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(actionFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
					for (UserAction a : aList) {
						bufferedWriter.write(a.user.username + "," + 
								a.actionType + "," + 
								a.actionData + "," +
								a.actionDate.toString() + "\n");
					}
					bufferedWriter.close();
					dataOutputStream.close();
					fileOutputStream.close();
				} catch (FileNotFoundException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Action file couln't be created: " + e.getMessage());
				} catch (IOException e) {
					Logger.getLogger("database").log(Level.SEVERE, "Action write error: " + e.getMessage());
				}
		} catch (IOException e) {
			Logger.getLogger("database").log(Level.SEVERE, "Action file couln't be created: " + e.getMessage());
		}		
	}


	public static void saveDatabaseData() {
		saveUsersToFile();
		savePurchaseDataToFile();
		saveUsageDataToFile();
		saveWasteDataToFile();
		saveSurveyDataToFile();
		saveUserActionDataToFile();
		
	}
}





