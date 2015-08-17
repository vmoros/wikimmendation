// when compiling or running, add "-cp json-simple-1.1.1.jar:." to your javac command
// first argument is directory that contains all category folders

// it goes through each category folder and adds a json file called "merged_cat.json"
// which is a merged dictionary of all json files in the category

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.io.FileWriter;
import java.io.File;
import java.util.Iterator;
import java.io.PrintWriter;
import java.lang.Long;

public class merge_cat {
  
  public static void main(String[] args) throws Exception {
    // Gets the directory parameters from terminal
    // delete all TOT_FREQ files
    // merge remaining files into dict
    File cat_folder = new File(args[0]);
    File[] cat_list = cat_folder.listFiles();
    
    JSONParser parser = new JSONParser();
    
    // for each category folder
    for (int i = 0; i < cat_list.length; i++) {
      System.out.println(cat_list[i].getName());
      File[] file_list = cat_list[i].listFiles();
      JSONObject merged_json = new JSONObject();
      
      // for each file in the current category folder
      for (int j = 0; j < file_list.length; j++) {
        //System.out.println(file_list[j].getName() + "Size: " + file_list[j].length());
        if (file_list[j].getName().equals("TOT_FREQ") || file_list[j].getName().equals("merged_cat.json") || file_list[j].length() == 0 || file_list[j].getName().equals("merged_cat_tfidf.json")) {
          file_list[j].delete();
        }
        else {
          String cur_file_str = new String(readAllBytes(get(file_list[j].getAbsolutePath())));
          JSONObject cur_json = (JSONObject) parser.parse(cur_file_str);
          
          // gets all keys from current json file
          Iterator<String> keys = cur_json.keySet().iterator();
          // loop through all keys
          while (keys.hasNext()) {
            String cur_key = keys.next();
            if (!(cur_key.equals("page_id")) && !(cur_key.equals("page_title")) && merged_json.containsKey(cur_key)) {
              long cur_value = (long) merged_json.get(cur_key);
              merged_json.put(cur_key, cur_value + 1);              
            }
            else {
              merged_json.put(cur_key, (long)1);
            }
            keys.remove();
          }
        }
      }

      JSONObject filtered_merged = filter_json(merged_json, 2.5);      
      
      FileWriter writer = new FileWriter(cat_list[i] + "/merged_cat.json");
      filtered_merged.writeJSONString(writer);
      writer.close();
            
    }
    
    
  }
  
  // Given a json object this function returns a json object with word occurences less than n removed
  public static JSONObject filter_json(JSONObject dict, double n) {
    // Set a threshold (only take the word if it appears more than 2 times)
    JSONObject rv = new JSONObject();
    Iterator<String> filter_keys = dict.keySet().iterator();
    
    while (filter_keys.hasNext()) {
      String cur_key = filter_keys.next();
      if ((cur_key.equals("page_id")) || (cur_key.equals("page_title")) || (long) dict.get(cur_key) > n) {
        rv.put(cur_key, dict.get(cur_key));
      }
    }
    
    return rv;
  }    
  
  
  
  
  
  
  
}
