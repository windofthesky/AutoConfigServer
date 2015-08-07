package configmanageserver;

import java.util.Timer;
import java.util.TimerTask;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @ClassName: ScheduledTimerTask.
 * @Description: this class extends from TimerTask and override the run() function which will be called when time's up.
 */
class ScheduledTimerTask extends TimerTask
{
    /**
     * @FieldName: ipaddr.
     * @Description: the ip address of the redis server which is used to store the parameter.
     */
    private String ipaddr;
	
    /**
     * @FieldName: channel.
     * @Description: the channel which is used to publish message of application.
     */
    private String channel;
	
    /**
     * @Title: setIPAddr.
     * @Description: the function which is used to assign ip to private ipaddr.
     * @param ip: the redis server ip address. 
     * @return none.
     */
    public void setIPAddr(String ip)
    {
	this.ipaddr = ip;
    }
	
    /**
     * @Title: setChannel.
     * @Description: the function which is used to assign channel.
     * @param chnl: the channel on the redis server. 
     * @return none.
     */
    public void setChannel(String chnl)
    {
	this.channel = chnl;
    }

        @Override
	public void run()
	{
		System.out.println("Time's up!!");
		Jedis jedis = new Jedis(ipaddr, 6379, 0);
		/*
		 * if user application has some monitor parameters,they should be got here 
		 * and then publish them to specific monitor channels. 
		 * the code shoule be as below:
		 *
		 * if(channel.equals("OnLine_UserNum"))
		 * {
		 *     String current_online_user_num = app.getOnlineUserNum();
		 *     jedis.publish(channel, current_online_user_num); //publish the online user num.
		 *     jedis.quit();
		 * } 
		 *
		 * if user application store the history data of the parameter,the redis command should 
		 * like these below:
		 * if(channel.equals("OnLine_UserNum"))
		 * {
		 *     String current_online_user_num = app.getOnlineUserNum();
		 *     String current_time = app.getCurrentTime();
		 *     jedis.hset("ONLINE_USERNUM", current_time, current_online_user_num);
		 *     jedis.quit();
		 * } 
		 *
		 */
		jedis.publish(channel, "this is a test!!!");
		jedis.quit();	
	}
}


/**
 * @ClassName: ScheduleTimer.
 * @Description: this class which is used to submit specific info according to the timer value.
 */
class ScheduleTimer 
{
    /**
     * @FieldName: timer.
     * @Description: the private java.util.Timer object of this class.
     */
    private Timer timer;
    
    /**
     * @Title: ScheduleTimer.
     * @Description: the construct function which is used to initialize the object.
     * @param ip:the ip address of the redis server.
     * @param channle: the pub/sub channel on the redis server.
     * @param time: the interval value of the timer. 
     * @return none.
     */
    public ScheduleTimer(String ip, String channel, int time)
    {
    	timer = new Timer();
    	ScheduledTimerTask timerTask = new ScheduledTimerTask();
    	timerTask.setIPAddr(ip);
    	timerTask.setChannel(channel);
    	
    	System.out.println("Timer Start!!!");
    	
    	//timer.schedule(new ScheduledTimerTask(), time*1000, time*3000);
    	timer.schedule(timerTask, time*1000, time*1000);
    }
}


/**
 * @ClassName: ConfigClient.
 * @Description: this class which is client of the config server,the shell of user application.
 */
public class ConfigClient
{
    /**
     * @Title: user_def_func.
     * @Description: the user defined function which is used to re-initialize the user application.
     * @return none.
     */
    private static void user_def_func()
    {
	//this function is defined by Client user,when get the latest config info
	//and then this function will be called to re-initialize the application config. 
    }
	
    /**
     * @Title: Subscriber.
     * @Description: the function which is used to subscribe one channel from redis server.
     * @param ip: the redis server ip address. 
     * @param channel: the specific channel which was subscribed by the application.
     * @return none.
     */
	private static void Subscriber(final String ip, final String channel)
	{
		final JedisPubSub jedisPubSub = new JedisPubSub() 
		{
			@Override	
			public void onMessage(String channel, String message) 
			{  
		        //System.out.println("Input channel =="+channel + "----input message ==" + message); 
		        
		        if(channel.equals("USER_SUBSCRIBE_CHANNEL"))
		        {
		        	user_def_func();
		        }
				//if user's application subscribe more than one channel and more user define functions
				//could be called here.
				/*
				if(channel.equals("USER_SUBSCRIBE_CHANNEL_another")
				{
				    user_def_func_another();
				}
				*/
				
		    }  
		};
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				//System.out.println("Start!!!"); 
				try 
				{
					Jedis jedis = new Jedis(ip, 6379, 0);  //0 means no timeout.
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
	
    /**
     * @Title: Publisher.
     * @Description: the function which is used to publish message to one channel on redis server.
     * @param ip: the redis server ip address. 
     * @param channel: the specific channel which the message will be published by the application.
     * @param message: the message which will be published to specific channel.
     * @return none.
     */
    private static void Publisher(String ip,String channel, String message)
    {
	Jedis jedis = new Jedis(ip, 6379, 0);
	jedis.publish(channel, message);
	jedis.quit();	
    }
	
    /**
     * @Title: main.
     * @Description: main function of the user application.
     * @param args: input arguments of the main function. 
     * @return none.
     */
    public static void main(String[] args)
    {
	Subscriber("10.1.1.178", "TEST");  //application subscribe one channel(named "TEST").
	Publisher("10.1.1.178", "TEST", "this is a test message!"); //application publish message to channel.
        ScheduleTimer pubTimer =new ScheduleTimer("10.1.1.178", "TEST", 5);
    }
}
