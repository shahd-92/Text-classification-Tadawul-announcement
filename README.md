# Tadawul announcements text processing and classification 
![Tadawul logo](https://www.dropbox.com/s/eem098fc4ez4rd0/Tadawul_Logo_FullColour_pos_rgb.png?raw=1)


##  Project Description:
This project starts by crawling the announcements in Saudi Stock Exchange **Tadawul** then applying the main text processing techniques like tokenizing, removing the stopping words, and stemming. Then it is building an inverted index to apply feature selection methods and text classification

##  Project Specifications:
The program starts by reading the goldstd.csv file, then opens each link and reads the title and the announcement. With each time it retrieves content it will tokenize it. After it reads all the contents it will remove the stopping words besides any word less than 3 letters, then apply stemming to them. By this step, the inverted index will be built as the previous assignment, so it will sort the index by IDF then it will write a CSV file where the columns are the attributes and the label, while the rows are the documents, and the values of attributes will be the documents frequencies.
I tried two features (attributes) selection methods, the first is selecting the 50 topmost IDF of the attributes, second is using the information gain method in Weka to select the 50 attributes. Also, I tried to select 2000 attributes using the IDF method, then select 50 attributes from the 2000 attributes using the IG method.
The program will read the CSV file and convert it to an ARFF file, then apply the feature selection method, after that, it will build two models (j-48 and SVM) with 260-fold cross-validation. Then it will display the evaluation metrics results, Accuracy, precision, recall, F measures.

## The results:
![results tabel](https://www.dropbox.com/s/g0jzgx98iswjjtn/Picture1.png?raw=1)
