package com.vivekranjan.geosocial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * Pulls Tweets based on specific geographic boundaries
 * and performs basic analysis on those tweets.
 * 
 * @author Vivek
 *
 */
public class TweetAnalyzer {
	
	private final static ch.qos.logback.classic.Logger log=
			(ch.qos.logback.classic.Logger)LoggerFactory.getLogger(TweetAnalyzer.class);
	
	/**
	 * Upper/northern latitude that marks the 
	 * upper bounds of the geographical area
	 * for which tweets need to be analysed. 
	 */
	private static double northLatitude;
	
	/**
	 * Lower/southern latitude. Marks the lower bound. 
	 */
	private static double southLatitude;
	
	/**
	 * Eastern/left longitude. Marks the left-side bounds.
	 */
	private static double eastLongitude;
	
	/**
	 * Western/right longitude. Marks the right-side bounds.
	 */
	private static double westLongitude;
	
	/**
	 * Amount of time passed since tweets were collected.
	 */
	final private static AtomicLong timePassed = new AtomicLong(0); 

	/**
	 * Entry point of the program.
	 * 
	 * @param args Command line arguments passed to the program.
	 */
	public static void main(String args[]) {
		setBounds(args);
		// Create the bounding box.
		double bb[][] = {{eastLongitude, southLatitude}
							,{westLongitude, northLatitude}};
		log.info("Bounding box has the following coordinates:"
				+ " ("+bb[0][0]+", "+bb[0][1]+") and ("
				+bb[1][0]+", "+bb[1][1]+")");
		
		FilterQuery fq = new FilterQuery();
		//fq.locations(bb);
		String keywords[]= {"varun gandhi"};
		fq.track(keywords);
		final List<Status> tweets = new ArrayList<Status>();
		// Initialise the twitter stream.
		final TwitterStream ts = (new TwitterStreamFactory()).getInstance();
		final long startTime = System.currentTimeMillis();
		// Listen for tweets.
		final AtomicBoolean shutDown = new AtomicBoolean(false);
		StatusListener listener = new StatusListener() {
			
			@Override
			public void onException(Exception arg0) {
				log.error(arg0.getMessage());
			}
			
			@Override
			public void onTrackLimitationNotice(int arg0) {
				log.error("Limit :"+arg0);
			}
			
			@Override
			public void onStatus(Status status) {
				tweets.add(status);
				
				// Quit listening after specified time 
				timePassed.set((System.currentTimeMillis() - startTime)/1000);
				log.trace("Time passed: "+timePassed.get());
				if(timePassed.get() >= 60*15) { 
					log.trace("Quitting listening after "+timePassed.get());
					shutDown.getAndSet(true);
					ts.shutdown();
				}
			}
			
			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
			}
		};
		ts.addListener(listener);
		ts.filter(fq);
		while(!shutDown.get()) {try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}};
		analyseTweets(tweets);
	}
	
	/**
	 * Analyse the tweets obtained.
	 * 
	 * @param tweets A list of tweets to be analysed.
	 */
	protected static void analyseTweets(List<Status> tweets) {
		log.trace("inside analyseTweets(). No. of tweets = "+tweets.size());
		log.info("Total Tweets : "+tweets.size());
		log.info("Total time passed : "+timePassed.get()+" seconds.");
		double minutes = timePassed.get() / 60;
		log.info("Average number of tweets per minute = "+(tweets.size()/minutes));
		Iterator<Status> tweetsIterator = tweets.iterator();
		TreeMap<String, Integer> hashtagCounts = new TreeMap<String, Integer>();
		TweetLogger tl = new TweetLogger();
		int geoTagged = 0;
		int loc = 0;
		while(tweetsIterator.hasNext()) {
			Status tweet = tweetsIterator.next();
			tl.logTweet(tweet);
			if(null!=tweet.getUser().getLocation()) {
				if(tweet.getUser().getLocation().trim().length()>1)
				{loc++;}
			}
			if(null!=tweet.getGeoLocation()) {
				geoTagged++;
			}
			Pattern MY_PATTERN = Pattern.compile("#(\\w+|\\W+)");
			Matcher mat = MY_PATTERN.matcher(tweet.getText());
			while(mat.find()) {
				String hashTag = mat.group(1);
				hashTag = hashTag.toLowerCase();
				int count = 0;
				if(hashtagCounts.containsKey(hashTag)) { 
					count = hashtagCounts.get(hashTag);
				}
				hashtagCounts.put(hashTag, ++count);
			}
		}
		//sort map
		Comparator<String> ordering = Ordering.natural().onResultOf(Functions.forMap(hashtagCounts)).compound(Ordering.natural());
		ImmutableSortedMap<String, Integer> sortedHashTagCounts = ImmutableSortedMap.copyOf(hashtagCounts, ordering);
		log.info("Number of geoTagged tweets: "+geoTagged);
		log.info("Number of location filled profiles: "+loc);
		log.trace(""+sortedHashTagCounts.size());
		log.info("Hashtags and their counts:");
		for(Entry<String, Integer> e : sortedHashTagCounts.entrySet()) {
			log.info(e.getKey()+" - "+e.getValue());
		}
	}

	/**
	 * Sets bounds according to the parameters supplied.
	 * 
	 * @param args Command line arguments.
	 */
	private static void setBounds(String args[]) {
		log.trace("Inside setBounds()");
		if(args.length < 4) {
			log.error("Please provide the bounding cordinates.");
			System.exit(0);
		} else {
			for(String arg : args) {
				if(arg.startsWith("-lat1")) {
					northLatitude = 
							Double.parseDouble(
									arg.trim()
									.substring(6));
					log.trace("northLatitude = "+northLatitude);
				} else if(arg.startsWith("-lat2")) {
					southLatitude = 
							Double.parseDouble(
									arg.trim()
									.substring(6));
					log.trace("southLatitude = "+southLatitude);
				} else if(arg.startsWith("-lon1")) {
					eastLongitude = 
							Double.parseDouble(
									arg.trim()
									.substring(6));
					log.trace("eastLongitude = "+eastLongitude);
				} else if(arg.startsWith("-lon2")) {
					westLongitude = 
							Double.parseDouble(
									arg.trim()
									.substring(6));
					log.trace("westLongitude = "+westLongitude);
				}
			}
		}
		
	}

	
	
}
