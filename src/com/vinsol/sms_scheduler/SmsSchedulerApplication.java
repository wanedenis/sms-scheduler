/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/

package com.vinsol.sms_scheduler;

import java.util.ArrayList;
import com.vinsol.sms_scheduler.models.Contact;
import android.app.Application;

public class SmsSchedulerApplication extends Application {

	public static ArrayList<Contact> contactsList = new ArrayList<Contact>();
	public static boolean isDataLoaded = false;
}
