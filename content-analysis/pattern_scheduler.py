import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import make_pipeline
from sklearn.model_selection import train_test_split
import joblib
import schedule
import time
import os
import re
import csv
import nltk
from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
import json

nltk.download('punkt')
nltk.download('stopwords')

def store_ngrams_to_json(json_file_path, ngrams_tupples_list):
    try:
        with open(json_file_path, "r") as json_file:
            existing_ngrams_coefficients = json.load(json_file)
    except FileNotFoundError:
        existing_ngrams_coefficients = {}

    ngrams_coefficients_dict = {ngram: coefficient for ngram, coefficient in ngrams_tupples_list}

    for ngram, coefficient in ngrams_coefficients_dict.items():
        # if ngram not in existing_ngrams_coefficients: should it be overriden, or remain the same
        existing_ngrams_coefficients[ngram] = coefficient

    print(existing_ngrams_coefficients)
        
    with open(json_file_path, "w") as json_file:
        json.dump(existing_ngrams_coefficients, json_file, indent=4)


def get_last_files(nr_files, folder_path):
    if not os.path.isdir(folder_path):
        print("Not a valid dir")
        return
    
    files = os.listdir(folder_path) 
    files.sort(key=lambda x: os.path.getctime(os.path.join(folder_path, x)), reverse=True)
    return [f for f in files if f.lower().endswith('.csv')][:nr_files]   


def read_csv_files_with_time_pattern(directory):
    csv_files = get_last_files(10, directory)
    if csv_files is None or len(csv_files)< 1:
        print(f"No CSV found in directory {directory}")
        return
    
    csv_files =  [f for f in csv_files if f.lower().endswith('.csv')]

    dfs = []
    header = []
    
    for filename in csv_files:
        with open(directory + "/" + filename, 'r') as csvfile:
            csv_reader = csv.reader(csvfile)
            for index, row in enumerate(csv_reader):
                if (row == header):
                    print("Skipped row processing. Detected duplicate header");
                    continue

                if (index == 0):
                    header = row
                else:
                    dfs.append(row)
    
    return (header, dfs)

# # Function to retrain the model with new data
def train_model_with_new_data(directory, model_path, vectorizer_path, ngram_file_path):
    print(f"SCHEDULED")
    mail_csv_data = read_csv_files_with_time_pattern(directory)
    header = mail_csv_data[0]
    csvs = mail_csv_data[1]

    content_idx = header.index("content")
    action_idx = header.index("action")

    print(content_idx)

    model_data = [(csv[content_idx], csv[action_idx]) for csv in csvs]

    if len(model_data) < 10:
        print("Not enough data for model training")
        return
    
    messages = [(tuple[0].lower(), tuple[1]) for tuple in  model_data]
    print(messages)

    X, y = zip(*messages)

    # Convert labels to binary
    y = [1 if label == 'BLOCK' else 0 for label in y]

    # Split the dataset into training and test sets
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Create a CountVectorizer to convert messages into a matrix of word counts
    stop_words = stopwords.words('english')

    vectorizer = CountVectorizer(stop_words=stop_words,ngram_range=(1, 3))
    X_train_counts = vectorizer.fit_transform(X_train)
    X_test_counts = vectorizer.transform(X_test)

    # Train a logistic regression model
    clf = LogisticRegression()
    clf.fit(X_train_counts, y_train)

    # # Predict on the test set
    # y_pred = clf.predict(X_test_counts)

    # # Calculate accuracy
    # accuracy = accuracy_score(y_test, y_pred)
    # print(f"Accuracy: {accuracy}")

    # Extract patterns (words with high coefficients)

    feature_names = vectorizer.get_feature_names_out()

    print(clf)
    pattern_indices = clf.coef_[0].argsort()[-100:][::-1]  # Get indices of top 5 coefficients
    print(clf.coef_)

    patterns = [feature_names[idx] for idx in pattern_indices]
    # print("Top patterns:", patterns)
    # print("Top patterns values:", clf.coef_[0].argsort())

    top_words = [(feature_names[i], clf.coef_[0][i]) for i in pattern_indices]

    # Print the top 20 words and their coefficients
    for word, coef in top_words:
        print(f"{word}: {coef}")

    joblib.dump(clf, model_path)
    joblib.dump(vectorizer, vectorizer_path)
    store_ngrams_to_json(ngram_file_path, top_words)

mail_dir = "/app/report_service/reports/mails"
model_path = '/app/report_service/models/mails/model.pkl'
vectorizer_path = '/app/report_service/models/mails/vectorizer.pkl'
ngram_path = '/app/report_service/patterns/patterns.json'

schedule.every(15).minutes.do(train_model_with_new_data, mail_dir, model_path, vectorizer_path, ngram_path)

if not (os.path.exists(model_path) and os.path.exists(vectorizer_path)):
    schedule.run_all()

# Run the scheduler loop
while True:
    schedule.run_pending()
    time.sleep(1)  # Sleep for 1 second to avoid high CPU usage
