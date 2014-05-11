#!/usr/bin/python
import json
import tweepy
import re
from tweepy import Stream
from tweepy import OAuthHandler
from tweepy.streaming import StreamListener
from threading import Thread,local
from subprocess import call
from time import sleep

MAX_TWEETS = 100
api_key = 'M4840W4WWQEICuF8phOLw'
api_secret = 'NMWgDzxatXWsv9Mgvotnu0JYFkazYEkuR1oLSkuupeY'
access_token = '843583182-EpX04p7T23EJg498NiMsDHmAw0KQ0l9OrpLIvghb'
access_secret = 'X1WrvSSO2Gy7jQqofJvQyKo3rige3E7wa72JjckZRRk62'
hotpicks = ['obama','putin','USA','Ukraine','Crimea','GameOfThrones', 'MH370','China','snowden','NSA','Pulitzer']

class tweets():
	def __init__(self,num):
        	self.num = num
        def set(self,x):
        	self.num = x
        def get(self):
        	return self.num


class ProcessTwitter(Thread):
	LIMIT = MAX_TWEETS
	#num = local()
	#setattr(num,'tweets',0)

	def __init__(self):
		Thread.__init__(self)
		self.tweets = tweets(0)

	def getTrends(self, auth):
        	api = tweepy.API(auth)
        	# trends from USA using woeid
        	trendslist = api.trends_place(23424977)
        	data = trendslist[0]
        	trends = data['trends']
        	alltrends = [re.sub('#','',trend['name']) for trend in trends]
        	alltrends.extend(hotpicks)
        	alltrends = [trend.encode('ascii',errors='backslashreplace') for trend in alltrends]
        	trendsfile = open('trendsfile.txt','w')
        	for onetrend in alltrends:
                	trendsfile.write(onetrend + "\n")
        	trendsfile.close()
        	return alltrends

	def percent_done(self):
		return self.tweets.get()/float(self.LIMIT)*100

	def run(self):
		try:
			#ProcessTwitter.num.tweets = 0
 			auth = OAuthHandler(api_key,api_secret,1)
        		auth.set_access_token(access_token,access_secret)
        		alltrends = self.getTrends(auth)
        		twitterstream = Stream(auth,self.Listner(self))
        		#twitterstream.sample()
        		twitterstream.filter(track=alltrends,languages=['en'])

		except (KeyboardInterrupt, SystemExit):
        		print '\nKeyboard interruption'
	

	class Listner(StreamListener):
	
		def __init__(self, procTwitter):
			self.twitter = procTwitter

		def on_data(self,data):
        		decoded = json.loads(data)
			datafile = open('HadoopData.txt','a')
			datafile.write(decoded['user']['screen_name'])
			datafile.write('|')
			datafile.write(str(decoded['user']['followers_count']))
			datafile.write('|')
			text = decoded['text'].encode('ascii','ignore')
			text = text.replace('\n', ' ')
			datafile.write(text)
			datafile.write('|')
			hashtags =  decoded['entities']['hashtags']
        		for hashtag in hashtags:
                		datafile.write(hashtag['text'].encode('ascii','ignore'))
				datafile.write(' ')
			datafile.write('|')
			usermentions = decoded['entities']['user_mentions']
			for usermention in usermentions:
				if usermentions is not None and usermention['screen_name'] is not '':
					datafile.write(usermention['screen_name'])
					datafile.write(' ')
        		#print json.dumps(decoded, indent=4)
        		datafile.write('\n')
			
			#self.twitter.num.tweets = self.twitter.num.tweets + 1
        		#if self.twitter.num.tweets == self.twitter.LIMIT:
			self.twitter.tweets.set(self.twitter.tweets.get()+1)
			if self.twitter.tweets.get() ==  self.twitter.LIMIT:
				datafile.close()
				#call(['hdfs','dfs','-copyFromLocal','HadoopData.txt'])
				#call(['hdfs','dfs','-copyFromLocal','trendsfile.txt'])
                		return False
        		return True

		def on_error(self,status):
        		print status



