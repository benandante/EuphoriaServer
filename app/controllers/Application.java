package controllers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Food;
import models.Purchase;
import models.Survey;
import models.Usage;
import models.User;
import models.UserAction;
import models.UserUndoAction;
import models.Waste;
import play.Play;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Application extends Controller {

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm:ss");

	public static void index() {
		// render();

	}

	@Before
	public static void allowAccess() {
		response.setHeader("Access-Control-Allow-Origin", "*");
	}

	/**
	 * registers a new user if the values are correct
	 * 
	 * @param username
	 * @param password
	 * @param passwordRepeat
	 * @param email
	 */
	public static void register(String username, String password,
			String passwordRepeat, String email, String address, int age,
			int gender) {

		Validation.current().email(email);
		Validation.current().required(username);
		Validation.current().required(password);
		Validation.current().required(passwordRepeat);
		Validation.current().equals(password, passwordRepeat);
		Validation.current().required(email);
		Validation.current().required(address);

		User newUser = null;
		JsonObject jo = new JsonObject();
		if (!Validation.current().hasErrors()) {
			User alreadyUser = User.findById(username);
			if (alreadyUser == null) {
				newUser = registerNewUser();
				// generate token
				String token = UUID.randomUUID().toString(); // "new token";

				// cache token for a session
				Cache.set(token, newUser, "30min");
				jo.addProperty("value", "ok");
				jo.addProperty("token", token);
				jo.addProperty("username", username);
				jo.addProperty("email", newUser.email);
				jo.addProperty("address", newUser.address);
				jo.addProperty("gender", newUser.gender);
				jo.addProperty("age", newUser.age);
				renderJSON(jo);
			} else {
				jo.addProperty("value",
						"A user with this username already exists!");
				renderJSON(jo);
			}
		} else {
			jo.addProperty("value",
					"Please specify username and password. The e-mail must be valid.");
			renderJSON(jo);
		}
	}

	public static User registerNewUser() {
		String username = request.params.get("username");
		String password = request.params.get("password");
		String email = request.params.get("email");
		String address = request.params.get("address");
		int age = Integer.parseInt(request.params.get("age"));
		int gender = Integer.parseInt(request.params.get("gender"));

		User user = new User(username, email, password);
		user.address = address;
		user.age = age;
		user.gender = gender;
		user.save();
		return user;
	}

	/**
	 * login function
	 * 
	 * @param username
	 * @param password
	 */
	public static void login(String username, String password) {

		// check db for username and password
		User user = User.findById(username);
		if (user == null || !user.password.equals(password)) {
			badRequest();
		}

		// generate token
		String token = UUID.randomUUID().toString(); // "new token";

		// cache token for a session
		Cache.set(token, user, "30min");

		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		jo.addProperty("token", token);
		jo.addProperty("username", username);
		jo.addProperty("email", user.email);
		jo.addProperty("address", user.address);
		jo.addProperty("gender", user.gender);
		jo.addProperty("age", user.age);
		
		renderJSON(jo);
	}

	/**
	 * retrieves all food list as a json array from database
	 */
	public static void getFoodList(String token) {
		controlToken(token);

		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		JsonArray ja = new JsonArray();

		List<Food> foods = Food.findAll();
		for (Food food : foods) {
			JsonObject foodObj = new JsonObject();
			foodObj.addProperty("id", food.id);
			foodObj.addProperty("name", food.name);
			foodObj.addProperty("type", food.category);
			foodObj.addProperty("unit", food.unit);
			foodObj.addProperty("icon", food.icon);

			ja.add(foodObj);
		}

		jo.add("foods", ja);

		renderJSON(jo);
	}

	/**
	 * renders food icon of a given food as binary
	 * 
	 * @param id
	 */
	public static void getFoodImage(int id, String token) {
		controlToken(token);
		Food food = Food.findById(id);
		if (food != null) {
			File file = Play.getFile("images/" + food.icon);
			renderBinary(file);
		} else {
			returnInvalidRequest();
		}
	}

	/**
	 * returns user item list
	 * 
	 * @param token
	 */
	public static void getUserFoodList(String token) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();

			List<Purchase> list = Purchase.find("user.username = ? ",
					user.username).fetch();

			for (Purchase item : list) {
				addNewPurchaseToJson(ja, item);

			}

			jo.add("userList", ja);

			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	/**
	 * adds purchase information useful for local database and user
	 * 
	 * @param ja
	 * @param item
	 */
	private static void addNewPurchaseToJson(JsonArray ja, Purchase item) {
		JsonObject foodObj = new JsonObject();
		foodObj.addProperty("id", item.id);
		foodObj.addProperty("foodID", item.food.id);
		foodObj.addProperty("userID", item.user.username);
		foodObj.addProperty("amount", item.amount);
		foodObj.addProperty("used", item.used);
		foodObj.addProperty("wasted", item.wasted);
		foodObj.addProperty("status", item.status);
		foodObj.addProperty("date", dateFormatter.format(item.date));
		foodObj.addProperty("unit", item.food.unit);
		ja.add(foodObj);
	}

	/**
	 * returns a bad request
	 */
	public static void returnInvalidRequest() {
		badRequest();
	}

	/**
	 * saves new purchase data to database
	 * 
	 * @param foodId
	 * @param quantity
	 * @param token
	 */
	public static void buyFood(long foodId, int quantity, String token,
			int status) {
		controlToken(token);

		User user = Cache.get(token, User.class);

		Food food = Food.findById(foodId);
		// check food
		if (food == null || user == null) {
			returnInvalidRequest();
		}

		Purchase purchase = new Purchase(user, food, new Date(), quantity);
		purchase.status = status;
		purchase.save();

		// return new purchase
		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");

		JsonArray ja = new JsonArray();
		addNewPurchaseToJson(ja, purchase);

		jo.add("userList", ja);
		renderJSON(jo);
	}

	/**
	 * updates the amount of food, is called by user action
	 * 
	 * @param foodId
	 * @param date
	 * @param quantity
	 * @param token
	 */
	public static void updateFoodAmount(long foodId, String date, int quantity,
			String token) {
		controlToken(token);

		User user = Cache.get(token, User.class);

		Purchase current = findPurchaseWithUserFoodDate(foodId, date, user);
		if (current != null) {
			// return new purchase
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");

			current.amount = quantity;
			current.save();

			JsonArray ja = new JsonArray();
			addNewPurchaseToJson(ja, current);

			jo.add("userList", ja);
			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	/**
	 * finds the purchase with given multi column primary keys
	 * 
	 * @param foodId
	 * @param date
	 * @param user
	 * @return
	 */
	private static Purchase findPurchaseWithUserFoodDate(long foodId,
			String date, User user) {
		List<Purchase> purchase;
		purchase = Purchase.find("user.username = ? and  food.id = ?",
				user.username, foodId).fetch();
		// check food
		if (purchase == null || user == null || purchase.size() == 0) {
			return null;
		}
		Purchase current = null;
		for (Purchase purchase2 : purchase) {
			if (dateFormatter.format(purchase2.date).equals(date)) {
				current = purchase2;
			}
		}
		return current;
	}

	/**
	 * controls whether given token is valid
	 * 
	 * @param token
	 */
	private static void controlToken(String token) {
		if (!(token != null && token.length() > 0 && Cache.get(token) != null)) {
			// not allowed
			returnInvalidRequest();
		}
	}

	/**
	 * saves a new waste entry
	 * 
	 * @param foodId
	 * @param quantity
	 * @param token
	 */
	public static void wasteFood(long foodId, double quantity, String token,
			String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);

		// if purchase is found
		if (purchase == null) {
			returnInvalidRequest();
		} else {
			Waste waste = new Waste(purchase, quantity, new Date(), 0);
			waste.save();
			purchase.wasted += quantity;
			purchase.save();

			// return new waste
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");

			JsonArray jarray = new JsonArray();
			addWasteToJson(jarray, waste);
			jo.add("wasteList", jarray);
			renderJSON(jo);
		}
	}

	/**
	 * adds the waste information useful for local database and the user
	 * 
	 * @param ja
	 * @param waste
	 */
	private static void addWasteToJson(JsonArray ja, Waste waste) {
		if (waste.deleted != Waste.STATUS_DELETED) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", waste.id);
			obj.addProperty("purchaseID", waste.purchase.id);
			obj.addProperty("amount", waste.amount);
			obj.addProperty("type", waste.wasteType);
			obj.addProperty("date", dateFormatter.format(waste.date));
			ja.add(obj);
		}
	}

	/**
	 * returns users' waste list
	 * 
	 * @param token
	 */
	public static void getUserWasteList(String token) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			List<Waste> list = Waste.find("purchase.user.username = ? ",
					user.username).fetch();

			for (Waste item : list) {
				addWasteToJson(ja, item);
			}

			jo.add("wasteList", ja);

			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	/**
	 * saves a new usage record into the dabatase
	 * 
	 * @param purchaseId
	 * @param quantity
	 * @param relationType
	 * @param token
	 */
	public static void consumeFood(long foodId, double quantity, String token,
			String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);

		// if purchase is found
		if (purchase == null) {
			returnInvalidRequest();
		} else {
			Usage usage = new Usage(purchase, quantity, new Date());
			usage.save();
			purchase.used += quantity;
			purchase.save();
			// return new usage
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addNewUsageToJson(ja, usage);
			jo.add("usageList", ja);
			renderJSON(jo);
		}
	}

	private static void addNewUsageToJson(JsonArray ja, Usage usage) {
		JsonObject obj = new JsonObject();
		obj.addProperty("id", usage.id);
		obj.addProperty("purchaseID", usage.purchase.id);
		obj.addProperty("amount", usage.amount);
		obj.addProperty("type", usage.relationType);
		obj.addProperty("number", usage.howManyPeople);
		obj.addProperty("date", dateFormatter.format(usage.date));
		ja.add(obj);

	}

	public static void getUserUsageList(String token) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			List<Usage> list = Usage.find("purchase.user.username = ? ",
					user.username).fetch();

			for (Usage item : list) {
				addNewUsageToJson(ja, item);
			}
			jo.add("usageList", ja);

			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	public static void updateUsageInformation(String token, long foodId,
			String purchaseDate, String usageDate, int relation, int number) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, purchaseDate,
				user);
		if (purchase == null) {
			returnInvalidRequest();
		}

		List<Usage> usageList = getUsageOfPurchase(purchase);

		Usage currentUsage = null;
		for (Usage usage : usageList) {
			if (dateFormatter.format(usage.date).equals(usageDate)) {
				currentUsage = usage;
			}
		}
		// if purchase is found
		if (currentUsage == null) {
			returnInvalidRequest();
		} else {
			currentUsage.howManyPeople = number;
			currentUsage.relationType = relation;
			currentUsage.save();

			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addNewUsageToJson(ja, currentUsage);
			jo.add("usageList", ja);
			renderJSON(jo);
		}
	}

	private static List<Usage> getUsageOfPurchase(Purchase purchase) {
		List<Usage> usageList = Usage.find("purchase.id = ?", purchase.id)
				.fetch();
		return usageList;
	}

	public static void updateWasteInformation(String token, long foodId,
			String purchaseDate, String wasteDate, int type) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, purchaseDate,
				user);
		if (purchase == null) {
			returnInvalidRequest();
		}

		List<Waste> wasteList = getWasteOfPurchase(purchase);

		Waste currentWaste = null;
		for (Waste w : wasteList) {
			if (dateFormatter.format(w.date).equals(wasteDate)) {
				currentWaste = w;
			}
		}
		// if purchase is found
		if (currentWaste == null) {
			returnInvalidRequest();
		} else {
			currentWaste.wasteType = type;
			currentWaste.save();

			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addWasteToJson(ja, currentWaste);
			jo.add("wasteList", ja);
			renderJSON(jo);
		}
	}

	private static List<Waste> getWasteOfPurchase(Purchase purchase) {
		List<Waste> wasteList = Waste.find("purchase.id = ?", purchase.id)
				.fetch();
		return wasteList;
	}

	public static void survey(String token, int q1, int q2, int q3, int q4) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			Survey survey = new Survey(user, q1, q2, q3, q4, new Date());
			survey.save();
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			jo.addProperty("id", survey.id);
			jo.addProperty("q1", q1);
			jo.addProperty("q2", q2);
			jo.addProperty("q3", q3);
			jo.addProperty("q4", q4);
			jo.addProperty("date", dateFormatter.format(survey.date));
			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	public static void surveyData(String token) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			List<Survey> surveyList = Survey.find("user.username = ? ",
					user.username).fetch();
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray array = new JsonArray();
			for (Survey item : surveyList) {
				addSurveyToJson(array, item);
			}
			jo.add("surveyData", array);

			renderJSON(jo);
		} else {
			returnInvalidRequest();
		}
	}

	private static void addSurveyToJson(JsonArray array, Survey item) {
		JsonObject jo = new JsonObject();
		jo.addProperty("id", item.id);
		jo.addProperty("q1", item.Q1);
		jo.addProperty("q2", item.Q2);
		jo.addProperty("q3", item.Q3);
		jo.addProperty("q4", item.Q4);
		jo.addProperty("date", dateFormatter.format(item.date));
		array.add(jo);

	}

	public static void buyFoodOffline(String token, long foodId,
			double quantity, double usage, double waste, String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Food food = Food.findById(foodId);
		Date da = fromTextToDate(date);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			purchase = new Purchase(user, food, da, quantity);
		} else {
			purchase.amount = quantity;
		}
		purchase.used = usage;
		purchase.wasted = waste;
		purchase.save();
		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		JsonArray ja = new JsonArray();
		addNewPurchaseToJson(ja, purchase);
		jo.add("purchase", ja);
		renderJSON(jo);

	}

	public static void addWasteOffline(String token, long foodId, double waste,
			int reason, String date, String wasteDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		List<Waste> wList = getWasteOfPurchase(purchase);
		if (wList == null || wList.size() == 0) {
			// add new waste
			Date d = fromTextToDate(wasteDate);
			Waste wasteToAdd = new Waste(purchase, waste, d, reason);
			wasteToAdd.save();
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addWasteToJson(ja, wasteToAdd);
			jo.add("wasteList", ja);
			renderJSON(jo);
		} else {
			// update waste
			Waste currentWaste = null;
			for (Waste w : wList) {
				if (dateFormatter.format(w.date).equals(wasteDate)) {
					currentWaste = w;
				}
			}
			// if purchase is found
			if (currentWaste == null) {
				returnInvalidRequest();
			} else {
				currentWaste.wasteType = reason;
				currentWaste.save();

				JsonObject jo = new JsonObject();
				jo.addProperty("value", "ok");
				JsonArray ja = new JsonArray();
				addWasteToJson(ja, currentWaste);
				jo.add("wasteList", ja);
				renderJSON(jo);
			}
		}
	}

	public static void addUsageOffline(String token, long foodId, double usage,
			int people, int relationship, String date, String usageDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		List<Usage> wList = getUsageOfPurchase(purchase);
		if (wList == null || wList.size() == 0) {
			// add new usage
			Date d = fromTextToDate(usageDate);
			Usage usageToAdd = new Usage(purchase, usage, d);
			usageToAdd.howManyPeople = people;
			usageToAdd.relationType = relationship;
			usageToAdd.save();
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addNewUsageToJson(ja, usageToAdd);
			jo.add("usageList", ja);
			renderJSON(jo);
		} else {
			// update
			Usage currentU = null;
			for (Usage w : wList) {
				if (dateFormatter.format(w.date).equals(usageDate)) {
					currentU = w;
				}
			}
			// if purchase is found
			if (currentU == null) {
				returnInvalidRequest();
			} else {
				currentU.howManyPeople = people;
				currentU.relationType = relationship;
				currentU.save();

				JsonObject jo = new JsonObject();
				jo.addProperty("value", "ok");
				JsonArray ja = new JsonArray();
				addNewUsageToJson(ja, currentU);
				jo.add("usageList", ja);
				renderJSON(jo);
			}
		}
	}

	private static Date fromTextToDate(String wasteDate) {
		Date d = new Date();
		try {
			d = dateFormatter.parse(wasteDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * called when user deletes a purchase item from the list
	 * 
	 * @param token
	 * @param foodId
	 * @param date
	 */
	public static void deletePurchase(String token, long foodId, String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		purchase.status = Purchase.STATUS_DELETED;
		purchase.deletionDate = new Date();
		purchase.save();

		Waste waste = null;
		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		JsonArray ja = new JsonArray();
		if (purchase.getRemainingAmount() != 0) {
			// possible waste
			try {
				waste = new Waste(purchase, purchase.getRemainingAmount(),
						dateFormatter.parse(date), 0);
				waste.save();
				addWasteToJson(ja, waste);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		jo.add("wasteList", ja);
		renderJSON(jo);
	}

	/**
	 * function is called when user deletes a waste item from the list
	 * 
	 * @param token
	 * @param foodId
	 * @param date
	 */
	public static void deleteWaste(String token, long foodId, String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}

		List<Waste> wasteList = getWasteOfPurchase(purchase);
		if (wasteList != null && wasteList.size() > 0) {
			Waste deletedWaste = wasteList.get(0);
			deletedWaste.deleted = Waste.STATUS_DELETED;
			deletedWaste.deletionDate = new Date();
			deletedWaste.save();
		} else {
			returnInvalidRequest();
		}
		renderSuccess();
	}

	/**
	 * deletes waste by offline action
	 * 
	 * @param token
	 * @param foodId
	 * @param purchaseDate
	 * @param deletionDate
	 */
	public static void deleteWasteOffline(String token, long foodId,
			String purchaseDate, String deletionDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, purchaseDate,
				user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		List<Waste> wasteList = getWasteOfPurchase(purchase);
		if (wasteList != null && wasteList.size() > 0) {
			Waste deletedWaste = wasteList.get(0);
			deletedWaste.deleted = Waste.STATUS_DELETED;
			try {
				deletedWaste.deletionDate = dateFormatter.parse(deletionDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			deletedWaste.save();
		} else {
			returnInvalidRequest();
		}
		renderSuccess();
	}

	/**
	 * deletes purchase with offline action
	 * 
	 * @param token
	 * @param foodId
	 * @param purchaseDate
	 * @param deletionDate
	 */
	public static void deletePurchaseOffline(String token, long foodId,
			String purchaseDate, String deletionDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, purchaseDate,
				user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		purchase.status = Purchase.STATUS_DELETED;
		try {
			purchase.deletionDate = dateFormatter.parse(deletionDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		purchase.save();

		renderSuccess();
	}

	/**
	 * sets survey data with offline action
	 * 
	 * @param token
	 * @param q1
	 * @param q2
	 * @param q3
	 * @param q4
	 * @param date
	 */
	public static void surveyOffline(String token, int q1, int q2, int q3,
			int q4, String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		if (user != null) {
			Survey survey = null;
			try {
				survey = new Survey(user, q1, q2, q3, q4,
						dateFormatter.parse(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			survey.save();
			renderSuccess();
		} else {
			returnInvalidRequest();
		}
	}

	/**
	 * buys item from shopping list and adds it to the available list
	 * 
	 * @param token
	 * @param foodId
	 * @param date
	 */
	public static void buyFromShoppingList(String token, long foodId,
			String date) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		purchase.status = Purchase.STATUS_AVAILABLE;
		try {
			purchase.date = dateFormatter.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		purchase.save();

		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		JsonArray ja = new JsonArray();
		addNewPurchaseToJson(ja, purchase);
		jo.add("purchase", ja);
		renderJSON(jo);
	}

	public static void buyFromShoppingListOffline(String token, long foodId,
			String date, String statusChangeDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if (purchase == null) {
			returnInvalidRequest();
		}
		purchase.status = Purchase.STATUS_AVAILABLE;
		try {
			purchase.date = dateFormatter.parse(statusChangeDate);
			purchase.statusChangeDate = purchase.date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		purchase.save();

		renderSuccess();
	}

	private static void renderSuccess() {
		JsonObject jo = new JsonObject();
		jo.addProperty("value", "ok");
		renderJSON(jo);
	}

	public static void userAction(String token, int actionType,
			String actionData, String actionDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		try {
			UserAction newAction = new UserAction(user, actionType, actionData,
					dateFormatter.parse(actionDate));
			newAction.save();
			renderSuccess();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			badRequest();
		}
	}

	/**
	 * changes user information
	 * 
	 * @param token
	 * @param email
	 * @param address
	 * @param age
	 * @param gender
	 */
	public static void editUserData(String token, String username, String email, String address,
			int age, int gender) {
		
		User user = User.findById(username); 
		Validation.current().email(email);
		Validation.current().required(email);
		Validation.current().required(address);

		JsonObject jo = new JsonObject();
		if (!Validation.current().hasErrors()) {
			if (user != null) {
				user.email = email;
				user.address = address;
				user.age = age;
				user.gender = gender;
				user.save();
				jo.addProperty("value", "ok");
				jo.addProperty("token", token);
				jo.addProperty("username", user.username);
				jo.addProperty("email", user.email);
				jo.addProperty("address", user.address);
				jo.addProperty("gender", user.gender);
				jo.addProperty("age", user.age);
				renderJSON(jo);
			} else {
				jo.addProperty("value", "A user with this username does not exist!");
				renderJSON(jo);
			}
		} else {
			jo.addProperty("value",
					"Please specify an address. The e-mail must be valid.");
			renderJSON(jo);
		}
	}

	public static void deleteConsumption(String token, int foodId, String date, double amount, String consDate) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if(purchase != null) {
			List<Usage> usageList = getUsageOfPurchase(purchase);
			for (Usage usage : usageList) {
				if(Double.compare(usage.amount, amount) == 0 && dateFormatter.format(usage.date).equals(consDate )){
					purchase.used -= usage.amount;
					purchase.save();
					usage.delete();
					
					UserUndoAction undoAction = new UserUndoAction(user, UserUndoAction.UNDO_CONSUMPTION);
					undoAction.purchase = purchase;
					undoAction.save();
					
					renderSuccess();
					break;
				}
			}
		}	
		returnInvalidRequest();
	}
	
	public static void undoFoodAmount(String token, int foodId, String date, double amount) {
		controlToken(token);
		User user = Cache.get(token, User.class);
		Purchase purchase = findPurchaseWithUserFoodDate(foodId, date, user);
		if(purchase != null) {
			purchase.amount = amount;
			purchase.save();
			UserUndoAction undoAction = new UserUndoAction(user, UserUndoAction.UNDO_AMOUNT_UPDATE);
			undoAction.purchase = purchase;
			undoAction.save();
			
			JsonObject jo = new JsonObject();
			jo.addProperty("value", "ok");
			JsonArray ja = new JsonArray();
			addNewPurchaseToJson(ja, purchase);
			jo.add("purchase", ja);
			renderJSON(jo);
		}
		returnInvalidRequest();
	}
	
	public static void saveDatabaseData() {
		DatabaseLoader.saveDatabaseData();
	}
}


