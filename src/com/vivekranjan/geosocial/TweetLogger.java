package com.vivekranjan.geosocial;

import org.slf4j.LoggerFactory;

import twitter4j.Status;

public class TweetLogger {
	
	private final static ch.qos.logback.classic.Logger log=
			(ch.qos.logback.classic.Logger)LoggerFactory.getLogger(TweetLogger.class);
	TweetLogger(){};
	public void logTweet(Status tweet) {
		log.info("@"+tweet.getUser().getScreenName()+": "+tweet.getText());
		log.info("@"+tweet.getUser().getScreenName()+":"+tweet.getUser().getDescription());
		if(null!=tweet.getUser().getLocation()) {
			if(tweet.getUser().getLocation().trim().length()>1)
			{log.info(tweet.getUser().getLocation());
			log.debug(tweet.getUser().getLocation());}
			else {log.info("nlc");}
		} else {
			log.info("nlc");
		}
		if(null!=tweet.getGeoLocation()) {
			log.info(tweet.getGeoLocation().getLatitude()+","+tweet.getGeoLocation().getLongitude());
		} else {
			log.info("-999,-999");
		}
	}
}
