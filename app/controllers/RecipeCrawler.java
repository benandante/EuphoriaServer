/**
 * 
 */
package controllers;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.libs.WS;
import play.mvc.Controller;


/**
 * @author F.Yalvac
 *
 */
public class RecipeCrawler extends Controller {

	public static void searchRecipe() {
		
		/////JsonElement jo =  WS.url("http://api.yummly.com/v1/api/recipes?_app_id=03a9402a&_app_key=84af32cab694affe89de78d1d2d4f464").get().getJson();
		 
			
		JsonElement jo =  WS.url("http://food2fork.com/api/search?key=0ef3e15ef34906e6b3df6d340d2cb724&q=shredded%20chicken").get().getJson();	
		
		Iterator<Entry<String, JsonElement>> iterator = jo.getAsJsonObject().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, JsonElement> type = (Entry<String, JsonElement>) iterator.next();
			System.out.println(type.getKey());
			if(type.getValue().isJsonArray()) {
				JsonArray arr = type.getValue().getAsJsonArray();
				for (int i = 0; i < arr.size(); i++) {
					if(arr.get(i).isJsonObject()) {
						JsonObject obb = arr.get(i).getAsJsonObject();
						Iterator<Entry<String, JsonElement>> iterator2 = obb.entrySet().iterator();
						while(iterator2.hasNext()) {
							Entry<String, JsonElement> val = iterator2.next();
							String key = val.getKey();
						//	System.out.println("\t" + key);
							if(key.equals("recipe_id")) {
								System.out.println(val.getValue());
								String url = "http://food2fork.com/api/get?key=0ef3e15ef34906e6b3df6d340d2cb724&rId=" + val.getValue().getAsString();
								System.out.println(url);
								JsonElement joRecipe =  WS.url(url).get().getJson();	
								renderJSON(joRecipe);
								Iterator<Entry<String, JsonElement>> iterator3 = joRecipe.getAsJsonObject().entrySet().iterator();
								/*while(iterator3.hasNext()) {
									Entry<String, JsonElement> val2 = iterator3.next();
									String key2 = val2.getKey();
									if(key2.equals("recipe")) {
										
										JsonArray recipe = val2.getValue().getAsJsonArray();
										if(recipe.size() > 0) {
											renderJSON(recipe);
										}
										System.out.println(recipe.size());
										for (int j = 0; j < recipe.size(); j++) {
											System.out.println("\t\t" + recipe.get(j).isJsonObject());
										}
										
									}
									
								}*/
							}
						}
					}
				}
				System.out.println(arr.size());
			}
			
		}
		renderJSON(jo);
	}
}


