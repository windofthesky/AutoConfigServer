/**
 * @Copyright to Hades.Yang 2015~2016.
 * @ClassName: ScheduledTimerTask.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The specific timer task which is used to implement timer event.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of ScheduledTimerTask was written.
 */

package autoconfigserver;
 
import java.util.Timer;
import java.util.TimerTask;

import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @ClassName: ScheduledTimerTask.
 * @Description: this class extends from TimerTask and override the run() function which will be called when time's up.
 */
public class ScheduledTimerTask extends TimerTask
{
    /**
     * @FieldName: configdb.
     * @Description: the ConfigServer database client.
     */
    private ConfigDBClient configdb;
	
    /**
     * @FieldName: channel.
     * @Description: the channel which is used to publish message of application.
     */
    private String channel;
	
	
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
	//System.out.println("Time's up!!");
	
        Jedis jedisConnector = null;
	boolean borrowOrOprSuccess = true;
	
	try 
	{
	    jedisConnector = configdb.db_client.getResource();
	    jedisConnector.publish(channel, "this is a test message");
	    /*
	     * if user application has some monitor parameters,they should be got here 
	     * and then publish them to specific monitor channels. 
	     * the code shoule be as below:
	     *
	     * if(channel.equals("OnLine_UserNum"))
	     * {
	     *     String current_online_user_num = app.getOnlineUserNum();
	     *     jedisConnector.publish(channel, current_online_user_num); //publish the online user num.
	     * } 
	     *
	     * if user application store the history data of the parameter,the redis command should 
	     * like these below:
	     * if(channel.equals("OnLine_UserNum"))
	     * {
	     *     String current_online_user_num = app.getOnlineUserNum();
	     *     String current_time = app.getCurrentTime();
	     *     jedisConnector.hset("ONLINE_USERNUM", current_time, current_online_user_num);
	     * } 
	     *
	     */
		
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
     * @Title: ScheduledTimerTask.
     * @Description: the construct function which is used to initialize the object.
     * @param serverinfo_0: the first redis server node ip & port information in the redis sentinel.
     * @param serverinfo_1: the second redis server node ip & port information in the redis sentinel.
     * @param serverinfo_2: the third redis server node ip & port information in the redis sentinel.
     * @return none.
     */
    public ScheduledTimerTask(String serverinfo_0, String serverinfo_1, String serverinfo_2)
    {
	this.db = new ConfigDBClient(serverinfo_0, serverinfo_1, serverinfo_2);
    }
}
