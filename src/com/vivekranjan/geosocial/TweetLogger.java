package com.vivekranjan.geosocial;

import org.slf4j.LoggerFactory;

import twitter4j.Status;

public class TweetLogger {
	
	private final static ch.qos.logback.classic.Logger log=
			(ch.qos.logback.classic.Logger)LoggerFactory.getLogger(TweetLogger.class);
	TweetLogger(){};
	public void logTweet(Status tweet) {
		log.info("@"+tweet.getUser().getScreenName()+": "+tweet.getText());
	}
}
