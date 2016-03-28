/**
 * @Copyright to Hades.Yang 2015~2016.
 * @ClassName: Publisher.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The Publisher class for AutoConfigServer.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of Publisher was written.
 */

//package name.
package autoconfigserver; 
 
//import for java utilities.
import java.util.Calendar;

//import for jedis components.
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * @ClassName: Publisher.
 * @Description: this class which is used to publish config info to the ConfigClient.
 */
public class Publisher
{ 
    /**
     * @FieldName: dbClient.
     * @Description: the ConfigServer database client.
     */
    private ConfigDBClient dbClient;
	
    /**
     * @FieldName: publishChannel.
     * @Description: the publish channel which the user config client listen to.
     */
    protected String publishChannel;
	
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
	    Jedis jedisConnector = null;
	    boolean borrowOrOprSuccess = true;
		
	    try 
	    {
  	        jedisConnector = dbClient.db_client.getResource();
		jedisConnector.publish(publishChannel, config);
	
		//Store the config into redis server.
	        Calendar calendar = Calendar.getInstance();
		
   	        //Sub_key is timestamp(for historical graph)
	        String store_time = String.valueOf(calendar.getTime());  
		
	        //Main_key is publish channel.
	        jedisConnector.hset(publishChannel, store_time, config); 
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
    }
	
    /**
     * @Title: Publisher.
     * @Description: the construct function which is used to initialize the object.
     * @param ip:the ip address of the redis server.
     * @param channle: the pub/sub channel on the redis server.
     * @return none.
     */
    public Publisher(String serverinfo_0, String serverinfo_1, String serverinfo_2, String channel)
    {
	this.configdb = new ConfigDBClient(serverinfo_0, serverinfo_1, serverinfo_2);
	this.publishChannel = channel;
    }
}
