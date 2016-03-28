/**
 * @Copyright to Hades.Yang 2015~2016.
 * @ClassName: ConfigMonitor.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The MonitorTool for AutoConfigServer.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of ConfigMonitor was written.
 */

//package name.
package autoconfigserver;

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
    private Subscriber sub;
	
    /**
     * @FieldName: pub.
     * @Description: the publisher instance for monitor send the config info to the ConfigClient.
     */
    private Publisher pub;
	
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
	sub = new Subscriber("10.1.1.41:6379", "10.1.1.41:6379", "10.1.1.41:6379", "TEST");
	pub = new Publisher("10.1.1.41:6379", "10.1.1.41:6379", "10.1.1.41:6379", "TEST");
	
	pub.sendConfig("this is a test!");
    }
}
