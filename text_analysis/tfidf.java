// when compiling or running, add "-cp json-simple-1.1.1.jar:." to your command

// the first argument is the directory containing all json frequency files
// the second argument is the json file containing word counts for the whole corpus
// the third argument is the destination of the resulting TFIDF json files

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.io.FileWriter;
import java.io.File;
import java.util.Iterator;
import java.io.PrintWriter;

public class tfidf {
  public static void main(String[] args) throws Exception {
    // read in user's arguments
    File json_source = new File(args[0]);
    String merged_source = args[1];
    File destination = new File(args[2]);
    File[] json_files = json_source.listFiles();
    
    JSONParser parser = new JSONParser();
    
    // read in merged dictionary as json object
    String merged_str = new String(readAllBytes(get(merged_source)));
    JSONObject merged = (JSONObject) parser.parse(merged_str.trim());
    
    // calculate number of files and log of number of files (will be used in tfidf calculations)
    int num_files = json_files.length;
    double log_num_files = Math.log10(num_files);
    
    // go to each file in the source directory and write a corresponding
    // file in the destination directory
    for (int i = 0; i < num_files; i++) {
      // progress tracker
      if (i % 10000 == 0) {
        double pct = ((double)i / num_files) * 100;
        System.out.println(pct + "% done");
      }
      
      // read in current json file as json object
      String cur_json_str = new String(readAllBytes(get(json_files[i].getAbsolutePath())));
      JSONObject cur_json = (JSONObject) parser.parse(cur_json_str.trim());
      
      // create new jsonobject to store target file
      JSONObject target_json = new JSONObject();
      
      // go through each key in the source json file and write that key and
      // its corresponding value into the target json object
      Iterator<String> keys = cur_json.keySet().iterator();
      while (keys.hasNext()) {
        String cur_key = keys.next();
        //System.out.println(cur_key);
        //System.out.println(cur_json.get(cur_key));
        if (cur_key.equals("page_id") || cur_key.equals("page_title")) {
          target_json.put(cur_key, cur_json.get(cur_key));
        }
        else {
          long cur_value = (long) cur_json.get(cur_key);
          double tfidf_value = cur_value * (log_num_files - Math.log10((long) merged.get(cur_key)));
          target_json.put(cur_key, tfidf_value);
        }
      
      }
            
      // write to file on disk
      if (target_json.size() > 2.5) {
        FileWriter target_writer = new FileWriter(destination + "/" + json_files[i].getName());
        target_json.writeJSONString(target_writer);
        target_writer.flush();
        target_writer.close();
      } else {
        System.out.println("Empty file: " + json_files[i].getName());
      }
      
    }
    
  }
  
}
