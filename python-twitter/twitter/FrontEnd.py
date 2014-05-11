#!/usr/bin/python

from uuid import uuid4

from flask import Flask
from flask import jsonify
from flask import Module
from flask import request
from flask import render_template
from flask_flatpages import FlatPages

from ThreadTwitter import ProcessTwitter

#mod = Module(__name__,'Twitter')
app = Flask(__name__)

pages = FlatPages(app)

threads_dict = {}

@app.route('/')
def root():
	return 'Cannot access twitter directly, use app'


@app.route('/start')
def process_start():
	process_mod_name = 'ThreadTwitter'
	process_class_name = 'ProcessTwitter'
	thread_key = ''

	if threads_dict:
               for key in threads_dict.keys():
                       thread_key = key

	else:
		process_mod_obj = __import__('%s' % (process_mod_name), \
                                                fromlist=[process_class_name])
        	process_class_obj = getattr(process_mod_obj, process_class_name)
        	twitter = process_class_obj()
        	twitter.start()
		thread_key = str(uuid4())
		threads_dict[thread_key] = twitter

	percent = threads_dict[thread_key].percent_done()
	done = False
	if percent == 100 and not threads_dict[thread_key].is_alive():
		done = True
		threads_dict.clear()
	percent = round(percent,1)
	return jsonify(key=thread_key, percent=percent, done=done) 


@app.route('/progress')
def process_progress():
	thread_key = request.args.get('key', '', type=str)
	
	if not threads_dict:
		return jsonify(error='Start the process first')
	for keys in threads_dict.keys():
		if not thread_key == keys:
			return jsonify(error='Thread Key does not exist')

	percent = threads_dict[thread_key].percent_done()
	done = False
	if percent == 100 and not threads_dict[thread_key].is_alive():
		done = True
		threads_dict.clear()
	percent = round(percent,1)
	return jsonify(key=thread_key, percent=percent, done=done)

@app.route('/path:path/')
def page(path):
	page = pages.get_or_404(path)
	return render_template('progress.html', page=page)

app.debug = True
app.run(host='0.0.0.0')

