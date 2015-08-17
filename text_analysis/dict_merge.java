// when compiling or running, add "-cp .:json-simple-1.1.1.jar" to your command

// first argument is the directory containing all json frequency files
// second argument is the destination for the merged dictionary json file

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.io.FileWriter;
import java.io.File;
import java.util.Iterator;
import java.io.PrintWriter;

public class dict_merge {
  
  public static void main(String[] args) throws Exception {
    // Gets the directory parameters from terminal
    File source = new File(args[0]);
    File destination = new File(args[1]);
    File[] source_list = source.listFiles();    
    
    // initialize JSON tools
    JSONObject rv = new JSONObject();
    JSONParser parser = new JSONParser();

    // Keeping this for progress displaying    
    int num_files = source_list.length;
    
    // Initialize structures for keeping track of data
    int word_count[] = new int[num_files / 100];
    int index_count[] = new int[num_files / 100];
    String wc = "";
    String ic = "";
    
    // Loop through all files in source directory 
    for (int i = 0; i < source_list.length; i++) {
      // Read each file in as json
      String cur_file_str = new String(readAllBytes(get(source_list[i].getAbsolutePath())));
      JSONObject cur_json = (JSONObject) parser.parse(cur_file_str.trim());
      
      // Gets all keys from current json file
      Iterator<String> keys = cur_json.keySet().iterator();
      
      // Checks if the current key is in the running merged copy
      // Adds 1 if it finds the key already there, adds the key if it is not already there
      while (keys.hasNext()) {
        String cur_key = keys.next();
        if (!(cur_key.equals("page_id")) && !(cur_key.equals("page_title")) && rv.containsKey(cur_key)) {
          rv.put(cur_key, (int)rv.get(cur_key) + 1);
        }
        else {
          rv.put(cur_key, 1);
        }
      }
      
      if (i % 10000 == 0) {
        System.out.println("Currently " + (i / (double) num_files) * 100 + "% done.");
        System.out.println("There are " + rv.size() + " words currently in the merged JSON.\n");
      }
      
      if (i % 100 == 0) {
        wc += rv.size() + " ";
        ic += i + " ";
      }
      
    }
    
    // Write word count data
    System.out.println("Writing word count data");
    PrintWriter word_data = new PrintWriter(destination + "/word_data.txt"); 
    word_data.println(wc);
    word_data.println(ic);
    word_data.close();
    System.out.println("Finished writing word count data");
    
    // Create merged JSON file
    System.out.println("Trying to create the final merged JSON.");
    FileWriter writer = new FileWriter(destination + "/merged_dict.json");
    //StringWriter writer = new StringWriter(destination + "/merged_dict.json");
    rv.writeJSONString(writer);
    
    System.out.println("Initialized writer.");
    //String json_string = rv.toString();
    //System.out.println("Length: " + json_string.length());
    //writer.write(json_string);
    
    System.out.println("Flush writer.");
    writer.flush();
    
    System.out.println("Close writer.");
    writer.close();

  }
  
}
