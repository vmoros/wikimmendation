# Script converts given text files to JSON format with computed word frequencies

import time
import re
import json
import os
from nltk.corpus import stopwords
from nltk import stem

def txt_to_freq(in_filepath, out_filepath):
	with open(in_filepath, 'r') as f:
		page_title = f.readline()
		page_id = f.readline()
		txt_file = f.read()
	
	words = re.findall("[a-zA-Z]\w*", txt_file)

	frequencies = {}

	for word in words:
		if word not in stop:
			word_stem= stemmer.stem(word)
			if word_stem not in stop and word_stem not in additional_stop_words:
				frequencies[word_stem] = frequencies.get(word_stem,0) + 1
	
	frequencies['page_id']=page_id.strip()
	frequencies['page_title']=page_title.strip()
	
	with open(out_filepath, 'w') as f:
		json.dump(frequencies, f)

def test_stemmers(input_text):
	freq_snowball = {}
	freq_lancaster = {}
	words = re.findall("[a-zA-Z]\w*",input_text)
	
	input_text=input_text.encode('ascii','ignore')

	
	t0= time.time()
	for word in words:
		if word not in stop:
			stem_snowball = stemmer.stem(word)

			if stem_snowball not in stop and stem_snowball not in additional_stop_words:
				freq_snowball[stem_snowball] = freq_snowball.get(stem_snowball,0) + 1
	
	t1 = time.time()
	
	for word in words:
		if word not in stop:
			stem_lancaster = stemmer2.stem(word)

			if stem_lancaster not in stop and stem_lancaster not in additional_stop_words:
				freq_lancaster[stem_lancaster] = freq_lancaster.get(stem_lancaster,0) + 1
	
	t2 = time.time()
	
	i=0
	print ("time of snowball: " + str(t1-t0))
	print ("len of snowball dict" + str(len(freq_snowball.keys())))
	print ("top words of the snowball dict:" )
	for w in sorted(freq_snowball, key=freq_snowball.get, reverse=True):
		print w, freq_snowball[w]
		i = i+1
		if i > 10:
			break	
	
	print "\n\n"
	
	i=0
	print ("time of lancaster: " + str(t2-t1))
	print ("len of lancaster dict" + str(len(freq_lancaster.keys())))
	print "top words of the lancaster dict: "
	for w in sorted(freq_lancaster, key=freq_lancaster.get, reverse=True):
		print w, freq_lancaster[w]
		i = i+1
		if i > 10:
			break	
	
	print ("length without stemming" + str(len(words)))

def create_all_json(in_folder, out_folder):
	
	for filename in os.listdir(in_folder):
		txt_to_freq(in_folder+'/'+filename, out_folder+'/'+filename[:-3]+ "json")


stemmer = stem.snowball.EnglishStemmer()
#stemmer2 = stem.lancaster.LancasterStemmer()

stop = stopwords.words('english')
additional_stop_words=['ref','http','sfn','blockquot','loc','date']

create_all_json("/home/kengel/Desktop/updated_wiki/txt_filtered/", "/home/kengel/Desktop/updated_wiki/json_frequencies/")




