/**
 * @Copyright to Hades.Yang 2015~2020.
 * @ClassName: Subscriber.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The subscriber class for AutoConfigServer.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of Subscriber was written.
 */
 
//package name.
package autoconfigserver; 
 
//import for java utilities.
import java.util.Calendar;

//import for jedis components.
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @ClassName: Subscriber.
 * @Description: this class which is used to get the monitor channel info from the ConfigClient.
 */
public class Subscriber 
{
	/**
     * @FieldName: dbClient.
     * @Description: the ConfigServer database client.
     */
	private ConfigDBClient dbClient;
	
	/**
     * @Title: Subscriber.
     * @Description: the construct function which is used to initialize the object.
	   * @param ip:the ip address of the redis server.
	   * @param channle: the pub/sub channel on the redis server.
     * @return none.
     */
	public Subscriber(String serverinfo_0, String serverinfo_1, String serverinfo_2, String channel, String channel) 
	{
		this.configdb = new ConfigDBClient(serverinfo_0, serverinfo_1, serverinfo_2);
		final JedisPubSub jedisPubSub = new JedisPubSub() 
		{
			@Override	
			public void onMessage(String channel, String message) 
			{
				Jedis jedisConnector = null;
		        boolean borrowOrOprSuccess = true;
				//System.out.println("Input channel =="+channel + "----input message ==" + message); 
			    //When get the message from ConfigClient,Store them into redis hash list. 
				Calendar calendar = Calendar.getInstance();
				
				//Sub_key is timestamp.
			    String store_time = String.valueOf(calendar.getTime());  
				
				//Main_hashkey is channel with "_SUB" at the end.
		        try 
		        {
			        jedisConnector = dbClient.db_client.getResource();
			        jedisConnector.hset(channel+"_SUB", store_time, message); 
		        }
		        catch(JedisConnectionException e)
		        {
			        borrowOrOprSuccess = false;
			        if(jedisConnector != null)
			        {
				        dbClient.db_client.returnBrokenResource(jedisConnector);
				        jedisConnector = null;
			        }
			        throw e;
		        }
		        finally
		        {
			        if(borrowOrOprSuccess && (jedisConnector!=null))
			        {
				        dbClient.db_client.returnResource(jedisConnector);
			        }
		        }
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
			        jedisConnector = dbClient.db_client.getResource();
			        jedisConnector.subscribe(jedisPubSub, channel);
		        }
		        catch(JedisConnectionException e)
		        {
			        borrowOrOprSuccess = false;
			        if(jedisConnector != null)
			        {
				        dbClient.db_client.returnBrokenResource(jedisConnector);
				        jedisConnector = null;
			        }
			        throw e;
		        }
		        finally
		        {
			        if(borrowOrOprSuccess && (jedisConnector!=null))
			        {
				        dbClient.db_client.returnResource(jedisConnector);
			        }
		        }
			}
		}, "subscriberThread").start();
	}
}
