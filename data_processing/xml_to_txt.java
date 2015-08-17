// Java script to convert XML formatted wikipedia articles into <article ID>.txt files

// to run: java xml_to_txt /path/to/input/xml/files /path/to/output/txt/files

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.util.regex.*;
import java.io.PrintWriter;
import java.io.File;

public class xml_to_txt {

  // Method which deletes all text after a given pattern we find in the XML files
  // This will be used to get rid of uninteresting text associated with references
  public static String delete_everything_after(String full, String to_remove){
    if (full.contains(to_remove)){
      int index = full.indexOf(to_remove);
      return full.substring(0, index);
    }
    else {
      return full;
    }
  }

  // This will read an xml file and write it to txt
  // We will call this on every file
  public static void readAndWriteOne(String filename, String destination) throws Exception {
  
    // read file into a string
    String whole = new String(readAllBytes(get(filename)));
    
    // initialize variables
    String title = "";
    String page_text = "";
    String page_id = "";
    
    // find page_id
    Pattern id_pattern = Pattern.compile("<id>.*?</id>");
    Matcher id_matcher = id_pattern.matcher(whole);
    if (id_matcher.find()){
      page_id = id_matcher.group();
      page_id = page_id.replaceFirst("<id>", "");
      page_id = page_id.replaceFirst("</id>", "");
    }
    
    // find page title
    Pattern title_pattern = Pattern.compile("<title>.*?</title>");
    Matcher title_matcher = title_pattern.matcher(whole);
    if (title_matcher.find()){
      title = title_matcher.group();
      title = title.replaceFirst("<title>", "");
      title = title.replaceFirst("</title>", "");
    }
       
    // find text
    int text_start = whole.indexOf("<text>");
    int text_end = whole.indexOf("</text>");
    page_text = whole.substring(text_start+6, text_end);
    
    // clean text
    
    // remove anything enclosed in {{}}
    page_text = page_text.replaceAll("(?s)\\{\\{.*?\\}\\}", "");
    // remove double brakets (we want to keep the text inside)
    page_text = page_text.replaceAll("(?s)\\[\\[", "");
    page_text = page_text.replaceAll("(?s)\\]\\]", "");

    // remove anything enclosed in []
    page_text = page_text.replaceAll("(?s)\\[.*?\\]", "");
    // remove anything enclosed in {||}
    page_text = page_text.replaceAll("(?s)\\{\\|.*?\\|\\}", "");
    // remove bars and replace with spaces
    page_text = page_text.replaceAll("(?s)\\|", " ");

    // remove anything starting and ending with = and containing notes or references
    page_text = delete_everything_after(page_text, "==References==");
    page_text = delete_everything_after(page_text, "===References===");
    page_text = delete_everything_after(page_text, "==Notes==");
    page_text = delete_everything_after(page_text, "===Notes===");
    page_text = delete_everything_after(page_text, "===Notes and References===");
    page_text = delete_everything_after(page_text, "==Notes and References==");
    
    
    // write to file
    PrintWriter writer = new PrintWriter(destination + page_id + ".txt");
    writer.println(title);
    writer.println(page_id);
    writer.println(page_text);
    writer.close();
    


    return;
  }

  public static void main(String[] args) throws Exception {
      
      File folder = new File(args[0]);
      File[] file_list = folder.listFiles();
      
      // Convert all files
      for (int i = 0; i < file_list.length; i++){
        String cur_file = file_list[i].getName();
        int name_len = cur_file.length();
        String file_ext = cur_file.substring(name_len - 3, name_len);
        
        if (file_ext.equals("xml")){
          readAndWriteOne(file_list[i].toString(), args[1]);
        }
       
      }
      

    return;
  }
}





