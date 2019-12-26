# Pandorka: Wikimmendation Engine

### Description
This was a group project for a computer science class at UChicago. We made a recommendation engine for Wikipedia. Unfortunately, we only ran it locally and did not deploy it to the web.

Initial Note:
  Our analysis involved heavy computation and data processing with large data sets, so we
  have not included our data in this repository. We have only included the data necessary 
  to run our final product. In this repository, you will find all of the code that we wrote
  and implemented to reach our final product.
  
####################################################################################################  

******************************************************
** DATA PROCESSING (see code in data_processing_a): **
******************************************************

  We started by downloading the XML dump from wikipedia's website. A link to this data can be found 
  here: http://meta.wikimedia.org/wiki/Data_dump_torrents#enwiki. We used the data from January 12th.
  Compressed, the XML file is 10.8 GB. Uncompressed, it is 51.3 GB. Due to the large and messy
  nature of this 50+ GB XML file, we needed to find a way to parse it and make the data more
  manageable. 
  
####################################################################################################
  (1) wikipedia.scala
  Our first processing step involved running the Scala program, "wikipedia.scala". We found this
  program online and most of it was written by someone else. Credit is given at the top of the
  source code. We hard coded this program to run in the same directory as the 51 GB XML file. On line
  19, we gave the file name of our Wikipedia XML dump. On line 22, we gave
  the folder for output file. The program works by parsing through the whole XML file,
  recognizing tags corresponding to new pages, and finally writing an XML file for each
  individual article. The newly created file is named after its Wikipedia ID number.
  
  To compile the program, Scala must be first installed. Then, run:
  
    scalac wikipedia.scala
    
  This creates a folder named xml in the current directory with the .class files. To run the
  program, run (in the directory of the wikipedia.xml and wikipedia.scala files)
  
    scala -cp . xml.wikipedia
  
  If everything is running smoothly, the program will print output for each file it creates. This
  program took 10 hours to parse the whole 51 GB XML file. We let it run overnight. 
  The output is 15.2 million individual XML files which take up approximately 80 GB of space. This
  program is also very taxing on RAM usage; we needed to run this on a machine with 32 GB of RAM.
####################################################################################################

####################################################################################################
  (2) filter.sh
  The second step is to filter these newly created XML files by size. We manually checked many 
  articles to get an idea of what sizes we should be keeping. A good lower limit to filter stubs
  but preserve normal articles seemed to be 10KB. For an upper limit, we found the size of the
  biggest Wikipedia article which isn't a list article. In our parsed XML data, that article corresponded
  to a 332KB XML file. We chose our upper limit as 333 KB in order to keep all non-list articles.
  The files larger than 333KB also included "Talk:" pages and "Wikipedia:" pages. These are administrative
  pages containing, for example, discussion of which articles to delete and which users to ban. Our
  upper threshold of 333KB therefore preserved all non-list articles. 
  
  A simple shell script performs this filtering by size. The shell script needs to know two things - 
  where the individual XML files are (/media/kengel/HDD/Wikidata/output/ in our case) and where
  the filtered data should be written to (/media/kengel/HDD/Wikidata/filtered_by_size/ in our case).
  Run the script after changing those directories by typing:
  
    sh filter.sh
   
  This script takes quite a long time to run (12+ hours, we ran it overnight). The result is
  a reduction from 15.2 million to ~850000 articles (~80 GB to ~23 GB).
####################################################################################################

####################################################################################################
  (3) filter_lists_disamb.R
  The third step is to filter these XML files based on article type. Certain articles types exist on 
  Wikipedia that we don't want to include in our recommended article list. Such artcle types are
  lists, disambiguation pages, administrative pages, talk pages, category pages, and template pages.
  We created and ran a program in R to handle this filtering called "filter_lists_disamb.R". To run
  this program, change line 5 to direct to the location of the XML files that have been filtered by size
  ("/media/kengel/HDD/Wikidata/filtered_by_size/" in our case). Then run as:
  
    Rscript filter_lists_disamb.R
    
  This program takes around 7 hours to complete. The result is a reduction to 655472 XML files (16 GB)
  which represent well-sized, clean, and ready-to-use Wikipedia articles. Although the reduction in size
  is drastic, we have preserved everything of interest, namely non-tiny non-list articles.
####################################################################################################

####################################################################################################
  (4) xml_to_txt.java
  The last step of the data processing portion is to convert the files from XML format to txt format.
  Doing this will allow us to later access the data more quickly and easily. Since we got tired of
  waiting so long for our programs to run, we decided to use a faster language for this step. We
  decided on Java. The program, "xml_to_txt.java" converts our filtered XML files to txt files. The 
  first line on each new file is the corresponding article's title, the second line is the article ID,
  and the remaining lines are the text of that article, with much formatting removed (tables, etc). The final txt
  file represent as pure of text as we found possible to extract from the messy original data. This
  program takes in as arguments firstly the path to the filtered XML files, and secondly, the folder 
  location of the soon-to-be output txt files. To run, first compile:
  
    javac xml_to_txt.java
  
  Then (in our case we ran, note that we changed hard drives since the last step):
  
    java xml_to_txt /media/kengel/Desktop/updated_wiki/filtered_by_size/ /media/kengel/Desktop/updated_wiki/txt_filtered/
  
  The result was a reduction in size to about 9 GB (the number of files stayed the same). This 
  was the last step that we took in data filtering and processing. The following steps correspond to 
  the files in the "text_analysis_b/" folder in our repository. 
  
####################################################################################################
####################################################################################################
  
*************************************************
** TEXT ANALYSIS (see code in text_analysis_b) **
*************************************************

  The following steps correspond to the textual analysis that we performed in order to categorize
  and connect our data.
  
####################################################################################################
  (1) txt_to_json.py
  We started by running a stemming algorithm which converts given text files into JSON formatted
  files with computed word-stem frequencies. For example, this step would convert the string
  "run runner running people computer" to "{run:3, people:1, computer:1}". We used python for
  this algorithm because of the stemming algorithms in the package NLTK. NLTK must be installed to run this program. 
  
  To run, specify input text file location and output json frequency file location. In our case, we
  changed line 95 to read:
  
    create_all_json("/home/kengel/Desktop/updated_wiki/txt_filtered/", "/home/kengel/Desktop/updated_wiki/json_frequencies/")
    
  Then we ran our program:
  
    python txt_to_json.py
    
  The result of running this program is a big reduction in size (~9 GB to ~5 GB) because we have
  essentially reduced each article to containing word stems and corresponding counts for those 
  stems. 
  
####################################################################################################
  (2) merge_cat.java
  Next, we scraped some data from Wikipedia. This might sound crazy since we just spent days downloading
  and cleaning full Wikipedia from scratch. However, Wikipedia has a "featured articles list", in which each
  of the approximately 4,500 articles is categorized. The problem is that the raw XML dump does not identify
  which articles are members of this featured list. We therefore scraped the featured articles in a way that
  preserved their categorization and used this data to categorize all articles. Towards that goal, we wrote
  the program "merge_cat.java" which, given a folder of already categorized articles (from featured articles), 
  computes a JSON file representing all JSON files in a given category combined together. It merges all articles
  in a given category into a "super article". In a later step, we compare each non-categorized article to each
  category's merged "super article" to judge how well that article fits in that category. This program
  adds a file called "merged_cat.json" to each category folder.
  
  To run this, we use a JSON .jar file, which must be included with a -cp when compiling. Compile like this from
  the text_analysis_b directory:
  
    javac -cp .:json-simple-1.1.1.jar merge_cat.java

  Then to run, the same -cp parameter must be used and the only argument is the directory that contains all
  category folders.
  In our case:
  
    java -cp .:json-simple-1.1.1.jar merge_cat /media/kengel/Desktop/updated_wiki/merged_categories/

####################################################################################################
  (3) dict_merge.java
  The next step was to create a merged dictionary with all words that appear in any article. Each key
  in the resulting dictionary is a stemmed word and its value is the number of articles in which it
  appears. We needed this information for a later step in which we use a  TFIDF (text frequency inverse
  document frequency) algorithm to compare any two articles. We then wrote this dictionary to
  a JSON file which ended up being about 98MB in size. The resulting file is named "merged_dict.json".
  It also outputs a file called "word_data.txt" which contains data about the number of words in the
  merged dictionary at several points in the process. We will use this data to make graphs for our
  presentation.

  To compile, the same -cp parameter from above must be specified:
    
    javac -cp .:json-simple-1.1.1.jar dict_merge.java

  To run, the -cp parameter must again be used. Also, the first argument is the directory containing all
  JSON files generated in step (1) of the text analysis process, and the second argument is the destination
  directory for the resulting JSON file. In our case:

    java -cp .:json-simple-1.1.1.jar dict_merge /media/kengel/Desktop/updated_wiki/json_frequencies/
                                    /media/kengel/Desktop/updated_wiki/

####################################################################################################
  (4) tfidf.java
  This step runs the TFIDF (text frequency inverse document frequency) algorithm on each json frequency
  file. This algorithm identifies which words in an article are the most important words for that article.
  It does this by penalizing the value associated each word for how frequently it appears in the entire corpus
  (all articles). For example, if an article contains many instances of a word that appears in very few other
  articles, that word's value will be very high. On the other hand, if an article contains many instances of a
  word that appears in many other article's, that word's value will be lower. The resulting files have
  the same names as the original JSON frequency files but are located in a different directory.

  To compile, the same -cp parameter must be specified:

    javac -cp .:json-simple-1.1.1.jar tfidf.java

  To run, the -cp parameter must be specified. Also, the first argument is the directory containing all
  json frequency files. The second argument is the json file containing word counts for the whole corpus
  (the file generated in step 3). The third argument is the destination of the resulting TFIDF json files.
  In our case:

    java -cp .:json-simple-1.1.1.jar tfidf /media/kengel/Desktop/updated_wiki/json_frequencies/
        /media/kengel/Desktop/updated_wiki/merged_dict.json /media/kengel/Desktop/updated_wiki/json_tfidf/

######################################################################################################
  (5) merge_cat_tfidf.java
  This step turns the merged dictionary for each category into a corresponding TFIDF dictionary. We
  again use this algorithm to find the most important words. This program adds a file called
  "merged_cat_tfidf.json" to each category folder.

  To compile, the same -cp parameter must be specified:

    javac -cp .:json-simple-1.1.1.jar merge_cat_tfidf.java

  To run, the -cp parameter must be specified. Also, the first argument is the directory that contains
  all the category folders. The second argument is the merged dictionary generated in step 3.
  The third argument is the total number of articles. In our case:

    java -cp .:json-simple-1.1.1.jar merge_cat_tfidf /media/kengel/Desktop/updated_wiki/merged_categories/
        /media/kengel/Desktop/updated_wiki/merged_dict.json 655472




#####################################################################################################
  (6) categorize.java
  This step is the final step of text analysis. It uses the information generated in the previous
  steps to categorize every article. It works by comparing a given article to the "super-articles"
  generated for each category and finding the category with the best fit for the article. It then
  copies the article's JSON file into the category's directory and adds a key indicating to which
  category the article was assigned.

  To compile, the same -cp parameter must be specified:

    javac -cp .:json-simple-1.1.1.jar categorize.java

  To run, the -cp parameter must be specified. Also, the first argument is the directory with all
  the files generated in step 4, which are the article JSON files with TFIDF applied. The second
  argument is the directory with all category directories. In our case:

    java -cp .:json-simple-1.1.1.jar categorize /media/kengel/Desktop/updated_wiki/json_tfidf/
        /media/kengel/Desktop/updated_wiki/merged_categories/
  
######################################################################################################

  The final result of all these steps is a directory containing category folders. Each category
  folder contains JSON files for each article that has been assigned to that category. These JSON
  files have been run through the TFIDF algorithm to make them suitable for comparison to each other.
  Each category folder also contains a file called "merged_cat_tfidf.json" which represents a
  combination of all featured articles in that category. 
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
