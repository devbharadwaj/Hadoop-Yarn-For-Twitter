#!/usr/bin/python
import tweepy
from tweepy import OAuthHandler

api_key = 'M4840W4WWQEICuF8phOLw'
api_secret = 'NMWgDzxatXWsv9Mgvotnu0JYFkazYEkuR1oLSkuupeY'
access_token = '843583182-EpX04p7T23EJg498NiMsDHmAw0KQ0l9OrpLIvghb'
access_secret = 'X1WrvSSO2Gy7jQqofJvQyKo3rige3E7wa72JjckZRRk62'


auth = OAuthHandler(api_key,api_secret,1)
auth.set_access_token(access_token,access_secret)
api = tweepy.API(auth)

for user in tweepy.Cursor(api.followers, screen_name="DevBhardwaj27").items():
	print user.screen_name
