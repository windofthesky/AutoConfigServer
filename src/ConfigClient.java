/**
 * @Copyright to Hades.Yang 2015~2020.
 * @ClassName: ConfigClient.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The Config Client for AutoConfigServer.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of ConfigClient was written.
 */

//package name.
package autoconfigserver;

//import for jedis components.
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @ClassName: ConfigClient.
 * @Description: this class which is client of the config server,the shell of user application.
 */
public class ConfigClient
{
    /**
     * @FieldName: configdb.
     * @Description: the ConfigServer database client.
     */
    private ConfigDBClient configdb;
	
    /**
     * @Title: ConfigDBInit.
     * @Description: this function is used to initialize the configdb.
     * @param serverinfo_0: the first redis server node ip & port information in the redis sentinel.
     * @param serverinfo_1: the second redis server node ip & port information in the redis sentinel.
     * @param serverinfo_2: the third redis server node ip & port information in the redis sentinel.
     * @return none.
     */
    private void ConfigDBInit(String serverinfo_0, String serverinfo_1, String serverinfo_2)
    {
	this.configdb = new ConfigDBClient(serverinfo_0, serverinfo_1, serverinfo_2);
    }
	
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
    protected void Subscriber(final String channel)
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
				Jedis jedisConnector = null;
		        boolean borrowOrOprSuccess = true;
		
		        try 
		        {
			        jedisConnector = configdb.db_client.getResource();
			        jedisConnector.subscribe(jedisPubSub, channel);
		        }
		        catch(JedisConnectionException e)
		        {
			        borrowOrOprSuccess = false;
			        if(jedisConnector != null)
			        {
				        configdb.db_client.returnBrokenResource(jedisConnector);
				        jedisConnector = null;
			        }
			        throw e;
		        }
		        finally
		        {
			        if(borrowOrOprSuccess && (jedisConnector!=null))
			        {
				        configdb.db_client.returnResource(jedisConnector);
			        }
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
     * @Note: this publisher function is also included into ScheduledTimerTask,which could be called when timer out event happen.
     */
    protected void Publisher(String ip,String channel, String message)
    {
	Jedis jedisConnector = null;
        boolean borrowOrOprSuccess = true;

        try 
        {
	    jedisConnector = configdb.db_client.getResource();
	    jedisConnector.publish(channel, message);
        }
        catch(JedisConnectionException e)
        {
	    borrowOrOprSuccess = false;
	    if(jedisConnector != null)
	    {
		configdb.db_client.returnBrokenResource(jedisConnector);
		jedisConnector = null;
	    }
	    throw e;
        }
        finally
        {
	    if(borrowOrOprSuccess && (jedisConnector!=null))
	    {
                configdb.db_client.returnResource(jedisConnector);
	    }
        }	
    }
	
    /**
     * @Title: main.
     * @Description: main function of the user application.
     * @param args: input arguments of the main function. 
     * @return none.
     */
    public static void main(String[] args)
    {
	ConfigClient cfg_client = new ConfigClient();
	cfg_client.ConfigDBInit("10.1.1.41:6379", "10.1.1.41:6379", "10.1.1.41:6379");
	cfg_client.Subscriber("TEST");
	cfg_client.Publisher("TEST", "this is a test");
        ScheduleTimer pubTimer =new ScheduleTimer("10.1.1.41:6379", "10.1.1.41:6379", "10.1.1.41:6379", "TEST", 5);
    }
}
