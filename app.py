# app.py
from flask import Flask, json, request, jsonify
import os
import urllib.request
from werkzeug.utils import secure_filename

app = Flask(__name__)

app.secret_key = "agrostock"

UPLOAD_FOLDER = 'static/uploads'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/')
def home():
    return 'Homepage'


@app.route('/upload', methods=['POST'])
def upload_file():
    # check if the post request has the file part
    if 'file' not in request.file:
        resp = jsonify({'message': 'No file part in the request'})
        resp.status_code = 400
        return resp

    file = request.file('file')

    errors = {}
    success = False

    for file in file:
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            success = True
        else:
            errors[file.filename] = 'File type is not allowed'

    if success:
        resp = jsonify({'File successfully uploaded'})
        resp.status_code = 301
        return resp
    else:
        resp = jsonify(errors)
        resp.status_code = 500
        return resp


if __name__ == '__main__':
    app.run(debug=True)
