package configserver;

import java.util.Calendar;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @ClassName: Subscriber.
 * @Description: this class which is used to get the monitor channel info from the ConfigClient.
 */
class Subscriber 
{
    /**
     * @FieldName: jedisClient.
     * @Description: the jedis client of redis server.
     */
    private Jedis jedisClient;
	
    /**
     * @Title: Subscriber.
     * @Description: the construct function which is used to initialize the object.
     * @param ip:the ip address of the redis server.
     * @param channle: the pub/sub channel on the redis server.
     * @return none.
     */
        public Subscriber(final String ip, final String channel) 
	{
		jedisClient = new Jedis(ip, 6379, 0);
		final JedisPubSub jedisPubSub = new JedisPubSub() 
		{
			@Override	
			public void onMessage(String channel, String message) 
			{
				//System.out.println("Input channel =="+channel + "----input message ==" + message); 
			    //When get the message from ConfigClient,Store them into redis hash list. 
				Calendar calendar = Calendar.getInstance();
				
				//Sub_key is timestamp.
			    String store_time = String.valueOf(calendar.getTime());  
				
				//Main_hashkey is channel with "_SUB" at the end.
			    jedisClient.hset(channel+"_SUB", store_time, message);  
		    }  
		};
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				System.out.println("Start!!!"); 
				try 
				{
					Jedis jedis = new Jedis(ip,6379,0);
					jedis.subscribe(jedisPubSub, channel);
					jedis.quit();
					jedis.close();
				} 
				catch (Exception e) 
				{
				    //e.printStackTrace();
				}
			}
		}, "subscriberThread").start();
	}
}

/**
 * @ClassName: Publisher.
 * @Description: this class which is used to publish config info to the ConfigClient.
 */
class Publisher
{ 
    /**
     * @FieldName: jedisClient.
     * @Description: the jedis client which connect the redis server.
     */
     protected Jedis jedisClient;
	
    /**
     * @FieldName: publishChannel.
     * @Description: the publish channel which the user config client listen to.
     */
     protected String publishChannel;
	
    /**
     * @Title: Publisher.
     * @Description: the construct function which is used to initialize the object.
     * @param ip:the ip address of the redis server.
     * @param channle: the pub/sub channel on the redis server.
     * @return none.
     */
    public Publisher(String ip, String channel)
    {
	jedisClient = new Jedis(ip, 6379, 0);
	publishChannel = channel;
    }
	
    /**
     * @Title: sendConfig.
     * @Description: the function which is used to send config info to the ConfigClient.
     * @param config: the config message which will be send to ConfigClient.
     * @return none.
     */
	protected void sendConfig(String config)
	{
		if(config != null)
		{
		    jedisClient.publish(publishChannel, config);
		    
		    //Store the config into redis server.
		    Calendar calendar = Calendar.getInstance();
			
			//Sub_key is timestamp(for historical graph)
		    String store_time = String.valueOf(calendar.getTime());  
			
			//Main_key is publish channel.
		    jedisClient.hset(publishChannel, store_time, config);  
		}
	}
}


/**
 * @ClassName: ConfigMonitor.
 * @Description: this class which monitor all the ConfigClient worked on the user application.
 */
public class ConfigMonitor
{
    /**
     * @FieldName: sub.
     * @Description: the subscriber instance for monitor fetch user application info.
     */
    private static Subscriber sub;
	
    /**
     * @FieldName: pub.
     * @Description: the publisher instance for monitor send the config info to the ConfigClient.
     */
    private static Publisher pub;
	
    /**
     * @Title: main.
     * @Description: main function of the config monitor.
     * @param args: input arguments of the main function. 
     * @return none.
     */
	public static void main(String[] args)
	{
		/**
		 * Subscriber is used to get monitor parameter from ConfigClient,which is running on the user application.
		 * For example:
		 * if ConfigMonitor want to monitor some parameter and the code should be written like below:
		 *
		 * CPU_LOAD = new Subscriber("10.1.1.178", "CPU_Load"); 
		 *
		 * if ConfigMonitor wand to config the ConfigClient,here should be a Publisher,code as below: 
		 * 
		 * Regist_Speed = new Publisher("10.1.1.178", "REGIST_SPEED");  //the ConfigClient will subscribe the channel.
		 *
		 * and if Administrator have a new config value to be sent to ConfigClient,code as below:
		 *
		 * Regist_Speed.sendConfig("500");  //Set regist speed to be 500/s. 
		 */
		sub = new Subscriber("10.1.1.178", "TEST");
		pub = new Publisher("10.1.1.178", "TEST");
		
		pub.sendConfig("this is a test!");
	}
}
