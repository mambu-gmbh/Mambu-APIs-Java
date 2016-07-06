package demo.cron;

import java.util.Calendar;
import java.util.Timer;

import demo.DemoUtil;

/**
 * Utility cron job class. Used to run the all demo test classes at the time specified in the confic.properties file.
 * Designed to run tests at the midnight of a day.
 * 
 * @author acostros
 *
 */

public class DemoTestCron {

	public static void main(String[] args) {

		Timer timer = new Timer();
		DemoTestRunner demoTestRunner = new DemoTestRunner();

		DemoUtil.setUp();

		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.HOUR_OF_DAY, DemoUtil.demoCronStartHour);
		startTime.set(Calendar.MINUTE, DemoUtil.demoCronStartMinute);
		startTime.set(Calendar.SECOND, DemoUtil.demoCronStartSecond);

		// set the timer to execute the task (DemoTestRunner) at the passed time (startTime)
		timer.schedule(demoTestRunner, startTime.getTime());

	}

}
