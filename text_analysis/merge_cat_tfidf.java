// when compiling or running, add "-cp json-simple-1.1.1.jar:." to your command
// first argument is the directory that contains all the cat folders
// second argument is the merged dictionary
// third argument is the total number of articles


// look at each json file, for key i change value i to (value i * log10(num_articles/merged(i)))

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.io.FileWriter;
import java.io.File;
import java.util.Iterator;
import java.io.PrintWriter;

public class merge_cat_tfidf {
  public static void main(String[] args) throws Exception {
    // read in user's arguments
    File cat_folder = new File(args[0]);
    File[] cat_list = cat_folder.listFiles();
    File total_merged_dict = new File(args[1]);
    int num_articles = Integer.parseInt(args[2]);
    double log_num_articles = Math.log10(num_articles);
    
    JSONParser parser = new JSONParser();
    
    // read in merged json
    String total_merged_str = new String(readAllBytes(get(total_merged_dict.getAbsolutePath())));
    JSONObject total_merged_json = (JSONObject) parser.parse(total_merged_str.trim());
    //System.out.println(total_merged_json.get("cleopatrato"));
    
    // look through each cat folder
    for (int i = 0; i < cat_list.length; i++) {
      System.out.println(cat_list[i].getName());
      File[] file_list = cat_list[i].listFiles();
      
      // look through each file in the current cat folder
      for (int j = 0; j < file_list.length; j++) {
        //System.out.println(file_list[j].getName());
        // find the merged_cat.json file
        if (file_list[j].getName().equals("merged_cat.json")) {
          String cur_merged_str = new String(readAllBytes(get(file_list[j].getAbsolutePath())));
          JSONObject cur_merged_json = (JSONObject) parser.parse(cur_merged_str);
          JSONObject target_json = new JSONObject();
          
          
          // loop through all keys
          Iterator<String> keys = cur_merged_json.keySet().iterator();
          while (keys.hasNext()) {
            String cur_key = keys.next();
            //System.out.println("cur_key is: " + cur_key);
            long cur_value = (long) cur_merged_json.get(cur_key);
            long total_merged_value = (long) 1;
            if (total_merged_json.containsKey(cur_key)) {
              total_merged_value = (long) total_merged_json.get(cur_key);
            }
            else {
              total_merged_value = (long) 10;
            }
            double tfidf_value = cur_value * (log_num_articles - Math.log10(total_merged_value));   
            target_json.put(cur_key, tfidf_value);
          }
          
          // write to file on disk
          FileWriter writer = new FileWriter(cat_list[i] + "/merged_cat_tfidf.json");
          target_json.writeJSONString(writer);
          writer.close();
          
        }
      }
      

      
    }
  
  }
  
}
