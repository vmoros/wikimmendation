// Arguments for this function:
// the first argument is the directory with all tfidf's in JSON format
// the second argument is the directory with all category directories

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;
//import javax.json.JsonWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.io.FileWriter;


public class categorize{
	
	public static void main(String[] args) throws Exception {
	  // All json tfidf's
		File source = new File(args[0]);
		
		// Folder with categories
		File cat_folder = new File(args[1]);
		File[] source_list = source.listFiles();
		
		// Get all names of category folders
		File[] cat_list = cat_folder.listFiles();
		
		// Start JSON parser
		JSONParser parser = new JSONParser();
		
		// Get number of files to loop through
		int num_files = source_list.length;
		
		// Keep track of category JSON
		JSONObject cat_json_list[] = new JSONObject[cat_list.length];
		for (int i = 0; i < cat_list.length; i++) {
		  Object obj = parser.parse(new FileReader(cat_list[i].getAbsolutePath() + "/merged_cat_tfidf.json"));
		  JSONObject current_json = (JSONObject) obj;
		  cat_json_list[i] = current_json;
		}
				
		// For each file
		for(int i=0; i<num_files; i++){
		  //System.out.println("current json file: " + source_list[i]);
		  
		  // For each category
			double best_score = 0.0;
      double this_score = 0.0;
		  
		  //long startTime = System.nanoTime();
		  
		  Object o = parser.parse(new FileReader(source_list[i].getAbsolutePath()));
		  JSONObject cur_art_json = (JSONObject) o;
		  //String cur_art_str = new String(readAllBytes(get(source_list[i].getAbsolutePath())));
      //JSONObject cur_art_json = (JSONObject) parser.parse(cur_art_str);
		  
		  //long endTime = System.nanoTime();
		  //double duration = (endTime - startTime) / (double)1e9;
		  //String s = String.valueOf(duration);
		  //System.out.println("Each read takes " + s + " seconds.");
		
		  File best_cat = cat_list[0];
		
		  for (int j=0; j< cat_list.length; j++){
//			File cat_file = new File (cat_folder+'merged_dict.json');
			//System.out.println(cat_list[i].getName());
			
			  JSONObject cur_cat_json = cat_json_list[j];
			
			  //String cur_cat_str = new String(readAllBytes(get(cat_list[j].getAbsolutePath()+ "/merged_cat_tfidf.json")));
		//	System.out.println(cur_file_str.substring(16380,16385));
			  //JSONObject cur_cat_json = (JSONObject) parser.parse(cur_cat_str.toString());
			 
			
			
//			Object obj = parser.parse(new FileReader(cat_list[i] + "/TOT_FREQ"));
	//		JSONObject cur_cat_json = (JSONObject) obj;
	      //long startTime = System.nanoTime();
			  this_score = cos_calc(cur_art_json, cur_cat_json);
			  //long endTime = System.nanoTime();
			  
			  //double duration = (endTime - startTime) / (double)1e9;
			  //String s = String.valueOf(duration);
			  
			  //System.out.println("Each cosine calc takes " + s + " seconds.");
			
			  if (this_score > best_score){
				  best_score = this_score;
				  best_cat = cat_list[j];				
			  }

		  }
		
    	// System.out.println("Article ID (" + source_list[i].getName() + ") matched category: " + best_cat.getName() + ", with score: " + best_score);
	    FileWriter writer = new FileWriter(best_cat + "/" + source_list[i].getName());
      cur_art_json.put("page_cat", best_cat.getName()); 
  	  cur_art_json.writeJSONString(writer);
    	writer.close();
	    //JsonWriter writer = new JsonWriter(best_cat + "/" + source_list[i].getName());
      //writer.writeObject(cur_art_json);
      //writer.close();	    
		

	  	if(i%100 ==0){
	  		System.out.println(i);
	  		//recategorize
  		}
	  }
		
		
	}

/* old_main computes correlations pairwise (was used for testing)

	public static void old_main(String[] args) throws Exception {
		
		JSONParser parser = new JSONParser();
		File source = new File(args[0]);
//		File destination = new File(args[1]);
		File[] source_list = source.listFiles(); 
		int num_files = source_list.length;

		for (int i = 0; i < source_list.length; i++) {
			System.out.print(i);
			try{
				Object obj = parser.parse(new FileReader((source_list[i].getAbsolutePath())));
				JSONObject dict1 = (JSONObject) obj;
				
				for (int j=0; j<source_list.length; j++){
			
					try {
	//					System.out.print((dict1));
					
						Object obj2 = parser.parse(new FileReader((source_list[j].getAbsolutePath())));
						JSONObject dict2 = (JSONObject) obj2;
						double rv = cos_calc(dict1,  dict2);
	//					System.out.println(rv);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e){
				e.printStackTrace();
			}
			
		}
		
	}

*/	

	public static double cos_calc(JSONObject dict1, JSONObject dict2){
		double d1_sq = 0.0;
    double d2_sq = 0.0;
		double d1_dot_d2 = 0.0;
		
		Iterator<String> keys = dict1.keySet().iterator();
		
		double d1_value;
		while (keys.hasNext()) {
			String cur_key = keys.next();
			if (!(cur_key.equals("page_id")) && !(cur_key.equals("page_title")) &&!(cur_key.equals("page_cat"))) {
				
				// Store the current value
	      d1_value = (double) dict1.get(cur_key);
		        		
				if (dict2.containsKey(cur_key)){
					d1_dot_d2 += d1_value * (double) dict2.get(cur_key);
				}
				
				d1_sq += Math.pow(d1_value, 2);
				//d1_sq += (double)dict1.get(cur_key) * (double)dict1.get(cur_key);
			}        
		}
		
		Iterator<String> keys2 = dict2.keySet().iterator();
		
		// Get the magnitude of dictionary 2
		while (keys2.hasNext()) {
			String cur_key = keys2.next();
			if (!(cur_key.equals("page_id")) && !(cur_key.equals("page_title")) && !(cur_key.equals("page_cat"))) {
				//d2_sq += Math.pow((double)dict2.get(cur_key), 2);
				d2_sq += Math.pow((double) dict2.get(cur_key), 2);
			}
		}
		
		return d1_dot_d2 / (Math.sqrt(d1_sq*d2_sq));
	}
	

}
// if you compile it's javac -cp json-simple.jar:. (thing_to_compile.java)
// to run java -cp json-simple.jar:. thing_to_compile










