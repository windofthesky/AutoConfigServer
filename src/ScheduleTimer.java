/**
 * @Copyright to Hades.Yang 2015~2016.
 * @ClassName: ScheduledTimer.
 * @Project: Auto Config Server.
 * @Package: autoconfigserver.
 * @Description: The timer class which use ScheduledTimerTask as the task.
 * @Author: Hades.Yang 
 * @Version: V1.0
 * @Date: 2015-08-12
 * @History: 
 *    1.2015-08-12 First version of ScheduledTimer was written.
 */

//package name. 
package autoconfigserver;
 
//import for java utilities.
import java.util.Timer;
import java.util.TimerTask;
 
 /**
 * @ClassName: ScheduleTimer.
 * @Description: this class which is used to submit specific info according to the timer value.
 */
public class ScheduleTimer 
{
    /**
     * @FieldName: timer.
     * @Description: the private java.util.Timer object of this class.
     */
    private Timer timer;
    
    /**
     * @Title: ScheduleTimer.
     * @Description: the construct function which is used to initialize the object.
     * @param serverinfo_0: the first redis server node ip & port information in the redis sentinel.
     * @param serverinfo_1: the second redis server node ip & port information in the redis sentinel.
     * @param serverinfo_2: the third redis server node ip & port information in the redis sentinel.
     * @param channle: the pub/sub channel on the redis server.
     * @param time: the interval value of the timer. 
     * @return none.
     */
    public ScheduleTimer(String serverinfo_0, String serverinfo_1, String serverinfo_2, String channel, int time)
    {
    	timer = new Timer();
    	ScheduledTimerTask timerTask = new ScheduledTimerTask(serverinfo_0, serverinfo_1, serverinfo_2);
    	timerTask.setChannel(channel);
    	
    	System.out.println("Timer Start!!!");
    	
    	//timer.schedule(new ScheduledTimerTask(), time*1000, time*3000);
    	timer.schedule(timerTask, time*1000, time*1000);
    }
}
