package application;

import java.util.HashMap;
import java.util.Map;

/**
 * MapStringConversion Class contains the static methods which is used for the
 * operations on Maps and Strings for convert between them.
 */
public class MapStringConversion {

	/**
	 * Converts The Map<String,String> to String.
	 * 
	 * @param The {@link Map} for convert it to String.
	 * @return The {@link String} which contains the map with keys and values.
	 */
	public static String convertMapToString(Map<String, String> map) {
		StringBuilder mapAsString = new StringBuilder("{");
		for (String key : map.keySet()) {
			mapAsString.append(key + "=" + map.get(key) + ", ");
		}
		mapAsString.delete(mapAsString.length() - 2, mapAsString.length()).append("}");
		return mapAsString.toString();
	}

	/**
	 * Converts The String to Map<String,String>.
	 * 
	 * @param The {@link String} which contains the map with keys and values.
	 * @return The {@link Map} for convert it to String.
	 */
	public static Map<String, String> convertStringToMap(String value) {
		value = value.substring(1, value.length() - 1); // remove curly brackets
		String[] keyValuePairs = value.split(","); // split the string to creat key-value pairs
		Map<String, String> map = new HashMap<>();

		for (String pair : keyValuePairs) // iterate over the pairs
		{
			String[] entry = pair.split("="); // split the pairs to get key and value
			map.put(entry[0].trim(), entry[1].trim()); // add them to the hashmap and trim whitespaces
		}
		return map;
	}
}
