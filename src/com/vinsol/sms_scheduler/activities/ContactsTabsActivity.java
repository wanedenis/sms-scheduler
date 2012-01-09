package com.vinsol.sms_scheduler.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.DBAdapter;
import com.vinsol.sms_scheduler.R;
import com.vinsol.sms_scheduler.models.MyContact;
import com.vinsol.sms_scheduler.models.SpannedEntity;
import com.vinsol.sms_scheduler.utils.Log;

public class ContactsTabsActivity extends Activity {
	
	TabHost tabHost;
	TabHost mtabHost;
	DBAdapter mdba = new DBAdapter(this);
	Cursor cur;
	LinearLayout listLayout;
	LinearLayout blankLayout;
	LinearLayout parentLayout;
	Button blankListAddButton;
	
	
	
	//---------------- Variables relating to Contacts tab -----------------------
	ListView nativeContactsList;
	Button doneButton;
	Button cancelButton;
	
	ContactsAdapter contactsAdapter;
	String origin;
	
	ArrayList<MyContact> selectedIds 	= new ArrayList<MyContact>();
	ArrayList<SpannedEntity> SpansTemp 	= new ArrayList<SpannedEntity>();
	//---------------------------------------------------------------------------
	
	
	
	//----------- Variables relating to Groups Tab-------------------------------
	ExpandableListView nativeGroupExplList;
	ExpandableListView privateGroupExplList;
	ArrayList<ArrayList<HashMap<String, Object>>> nativeChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	ArrayList<HashMap<String, Object>> nativeGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	
	ArrayList<ArrayList<HashMap<String, Object>>> privateChildDataTemp = new ArrayList<ArrayList<HashMap<String, Object>>>();
	ArrayList<HashMap<String, Object>> privateGroupDataTemp = new ArrayList<HashMap<String, Object>>();
	
	SimpleExpandableListAdapter nativeGroupAdapter;
	SimpleExpandableListAdapter privateGroupAdapter;
	
	boolean hasToRefresh;
	//-----------------------------------------------------------------------------
	
	
	
	
	//----------------------Variables relating to Recents Tab-----------------------
	ArrayList<Long> recentIds = new ArrayList<Long>();
	ArrayList<Long> recentContactIds = new ArrayList<Long>();
	ArrayList<String> recentContactNumbers = new ArrayList<String>();
	ListView recentsList;
	RecentsAdapter recentsAdapter;
	//------------------------------------------------------------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_tabs_layout);
		
		
		//----------------------Setting up the Tabs--------------------------------
		final TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        
        tabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.contacts_tabs_native_contacts_list);
        spec1.setIndicator("Contacts", getResources().getDrawable(R.drawable.contacts_tab_states));

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Groups", getResources().getDrawable(R.drawable.groups_tab_states));
        spec2.setContent(R.id.group_tabs);

        TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("Recents", getResources().getDrawable(R.drawable.recent_tab_states));
        spec3.setContent(R.id.contacts_tabs_recents_list);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg_selector));
        }
    	//----------------------------------------------------end of Tabs Setup-----------
        
        
        
		
		Intent intent = getIntent();
		origin = intent.getStringExtra("ORIGIN");
		
		if(origin.equals("new")){
			for(int i = 0; i < NewScheduleActivity.Spans.size(); i++){
				SpansTemp.add(NewScheduleActivity.Spans.get(i));
			}
			for(int groupCount = 0; groupCount< NewScheduleActivity.nativeGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, NewScheduleActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, NewScheduleActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, NewScheduleActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, NewScheduleActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);//NewScheduleActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< NewScheduleActivity.nativeChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) NewScheduleActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				nativeGroupDataTemp.add(group);
				nativeChildDataTemp.add(child);
			}
			
			for(int groupCount = 0; groupCount< NewScheduleActivity.privateGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);// NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< NewScheduleActivity.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				privateGroupDataTemp.add(group);
				privateChildDataTemp.add(child);
			}
			
		}else if(origin.equals("edit")){
			for(int i = 0; i < EditScheduledSmsActivity.Spans.size(); i++){
				SpansTemp.add(EditScheduledSmsActivity.Spans.get(i));
			}
			
			for(int groupCount = 0; groupCount< EditScheduledSmsActivity.nativeGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSmsActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSmsActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSmsActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSmsActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);// EditScheduledSmsActivity.nativeGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.d(EditScheduledSmsActivity.nativeChildData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSmsActivity.nativeChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) EditScheduledSmsActivity.nativeChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				nativeGroupDataTemp.add(group);
				nativeChildDataTemp.add(child);
			}
			
			
			
			
			for(int groupCount = 0; groupCount< EditScheduledSmsActivity.privateGroupData.size(); groupCount++){
				boolean hasAChild = false;
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, false);//EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.d(EditScheduledSmsActivity.privateChildData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSmsActivity.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
					if((Boolean) EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK)){
						hasAChild = true;
					}
				}
				if(hasAChild){
					group.put(Constants.GROUP_CHECK, true);
				}
				privateGroupDataTemp.add(group);
				privateChildDataTemp.add(child);
			}
		}
		
		
		
        doneButton			= (Button) 		findViewById(R.id.contacts_tab_done_button);
        cancelButton		= (Button) 		findViewById(R.id.contacts_tab_cancel_button);
        
        
        doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if(origin.equals("new")){
					NewScheduleActivity.nativeGroupData.clear();
					NewScheduleActivity.nativeChildData.clear();

					NewScheduleActivity.nativeGroupData = nativeGroupDataTemp;
					NewScheduleActivity.nativeChildData = nativeChildDataTemp;
					
					NewScheduleActivity.privateGroupData.clear();
					NewScheduleActivity.privateChildData.clear();

					NewScheduleActivity.privateGroupData = privateGroupDataTemp;
					NewScheduleActivity.privateChildData = privateChildDataTemp;
					
					NewScheduleActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						NewScheduleActivity.Spans.add(SpansTemp.get(i));
					}
				}else if(origin.equals("edit")){
					EditScheduledSmsActivity.nativeGroupData.clear();
					EditScheduledSmsActivity.nativeChildData.clear();
					
					EditScheduledSmsActivity.nativeGroupData = nativeGroupDataTemp;
					EditScheduledSmsActivity.nativeChildData = nativeChildDataTemp;
					
					EditScheduledSmsActivity.privateGroupData.clear();
					EditScheduledSmsActivity.privateChildData.clear();
					
					EditScheduledSmsActivity.privateGroupData = privateGroupDataTemp;
					EditScheduledSmsActivity.privateChildData = privateChildDataTemp;
					
					EditScheduledSmsActivity.Spans.clear();
					for(int i = 0; i< SpansTemp.size(); i++){
						EditScheduledSmsActivity.Spans.add(SpansTemp.get(i));
					}
				}
					
				setResult(2, intent);
				ContactsTabsActivity.this.finish();
			}
		});
        
        
        
        
        
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(2, intent);
				ContactsTabsActivity.this.finish();
			}
		});
        
        
        
        //------------------Setting up the Contacts Tab ---------------------------------------------
        nativeContactsList 	= (ListView) findViewById(R.id.contacts_tabs_native_contacts_list);
		contactsAdapter = new ContactsAdapter();
        nativeContactsList.setAdapter(contactsAdapter);
        //------------------------------------------------------------end of setting up Contacts Tab--------
        
        
        
        
        //---------------- Setting up the Groups Tab -----------------------------------------------
        parentLayout = (LinearLayout) findViewById(R.id.private_list_parent_layout);
        listLayout = (LinearLayout) findViewById(R.id.list_layout);
        blankLayout = (LinearLayout) findViewById(R.id.blank_layout);
        blankListAddButton = (Button) findViewById(R.id.blank_list_add_button);
        
        blankListAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsTabsActivity.this, GroupEditActivity.class);
				intent.putExtra("STATE", "new");
				startActivity(intent);
			}
		});
        
        mdba.open();
		cur = mdba.fetchAllGroups();
		if(cur.getCount()==0){
			listLayout.setVisibility(LinearLayout.GONE);
			blankLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			listLayout.setVisibility(LinearLayout.VISIBLE);
			blankLayout.setVisibility(LinearLayout.GONE);
		}
		mdba.close();
		
        LinearLayout groupTabs = (LinearLayout)findViewById( R.id.group_tabs );
        mtabHost = (TabHost)groupTabs.findViewById( android.R.id.tabhost );
        
        mtabHost.setup( );
        mtabHost.getTabWidget().setDividerDrawable(R.drawable.vertical_seprator);
        
        setupTab(new TextView(this), "Phone Groups");
    	setupTab(new TextView(this), "My Groups");

        privateGroupExplList = (ExpandableListView) findViewById(R.id.private_list);
        nativeGroupExplList = (ExpandableListView) groupTabs.findViewById(R.id.native_list);
        
        nativeGroupsAdapterSetup();
		privateGroupsAdapterSetup();
		
		nativeGroupExplList.setAdapter(nativeGroupAdapter);
		privateGroupExplList.setAdapter(privateGroupAdapter);
		//----------------------------------------------------end of Groups Tab setup-------------------
        
        
		
		//--------------------setting up Recents Tab--------------------------------
		recentIds.clear();
		recentContactIds.clear();
		recentContactNumbers.clear();
		recentsList = (ListView) findViewById(R.id.contacts_tabs_recents_list);
		mdba.open();
		Cursor cur = mdba.fetchAllRecents();
		if(cur.moveToFirst()){
			do{
				recentIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_ID)));
				recentContactIds.add(cur.getLong(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_CONTACT_ID)));
				recentContactNumbers.add(cur.getString(cur.getColumnIndex(DBAdapter.KEY_RECENT_CONTACT_NUMBER)));
			}while(cur.moveToNext());
		}
		recentsAdapter = new RecentsAdapter();
		recentsList.setAdapter(recentsAdapter);
		mdba.close();
	}
	
	

	private void setupTab(final View view, final String tag) {
		View tabview = createTabView(mtabHost.getContext(), tag);
		TabSpec setContent = null;
		if(tag.equals("Phone Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.native_list);
		}else if(tag.equals("My Groups")){
			setContent = mtabHost.newTabSpec(tag).setIndicator(tabview).setContent(R.id.private_list_parent_layout);
		}
		mtabHost.addTab(setContent);
	}

	
	
	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(privateGroupDataTemp.size() == 0){
			hasToRefresh = true;
		}else{
			hasToRefresh = false;
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(hasToRefresh){
			mdba.open();
			cur = mdba.fetchAllGroups();
			if(cur.getCount()==0){
				listLayout.setVisibility(LinearLayout.GONE);
				blankLayout.setVisibility(LinearLayout.VISIBLE);
			}else{
				listLayout.setVisibility(LinearLayout.VISIBLE);
				blankLayout.setVisibility(LinearLayout.GONE);
			}
			mdba.close();
			reloadPrivateGroupData();
			privateGroupAdapter.notifyDataSetChanged();
			hasToRefresh = false;
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		Intent intent = new Intent();
		setResult(2, intent);
		
		ContactsTabsActivity.this.finish();
	}
	
	
	
	//************************* Adapter for the list *****************************************
	//**************************** in Contacts Tab ********************************************
	
	class ContactsAdapter extends ArrayAdapter {
		
		ContactsAdapter(){
    		super(ContactsTabsActivity.this, R.layout.contacts_list_row_design, SmsApplicationLevelData.contactsList);
    	}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ContactsListHolder holder;
			if(convertView==null){
				LayoutInflater inflater = getLayoutInflater();
	    		convertView = inflater.inflate(R.layout.contacts_list_row_design, parent, false);
	    		holder = new ContactsListHolder();
				holder.contactImage 	= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
	    		holder.nameText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
	    		holder.numberText 		= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
	    		holder.contactCheck     = (CheckBox) convertView.findViewById(R.id.contact_list_row_contact_check);
	    		convertView.setTag(holder);
			}else{
				holder = (ContactsListHolder) convertView.getTag();
			}
    		holder.contactImage.setImageBitmap(SmsApplicationLevelData.contactsList.get(position).image);
    		holder.nameText.setText(SmsApplicationLevelData.contactsList.get(position).name);
    		holder.numberText.setText(SmsApplicationLevelData.contactsList.get(position).number);
    		
    		for(int i = 0; i< SpansTemp.size(); i++){
    			
        		if(Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id) == SpansTemp.get(i).entityId){
        			holder.contactCheck.setChecked(true);
        			break;
        		}else{
        			holder.contactCheck.setChecked(false);
        		}
        	}
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						boolean isPresent = false;
						for(int i = 0; i< SpansTemp.size(); i++){
							if(SpansTemp.get(i).entityId == Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id)){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							SpannedEntity span = new SpannedEntity(-1, 2, SmsApplicationLevelData.contactsList.get(position).name, Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id), -1);
							span.groupIds.add((long) -1);
							span.groupTypes.add(-1);
							SpansTemp.add(span);
						}
					}else{
						for(int i = 0; i<SpansTemp.size(); i++){
				    		if(Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id) == SpansTemp.get(i).entityId){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if(Long.parseLong((String)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID)) == SpansTemp.get(i).entityId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if(Long.parseLong((String)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID)) == SpansTemp.get(i).entityId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			SpansTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged(); 
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		
    		
    		
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						boolean isPresent = false;
						for(int i = 0; i< SpansTemp.size(); i++){
							if(SpansTemp.get(i).entityId == Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id)){
								isPresent = true;
								break;
							}
						}
						if(!isPresent){
							SpannedEntity span = new SpannedEntity(-1, 2, SmsApplicationLevelData.contactsList.get(position).name, Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id), -1);
							span.groupIds.add((long) -1);
							span.groupTypes.add(-1);
							SpansTemp.add(span);
						}
					}else{	
						holder.contactCheck.setChecked(false);
						for(int i = 0; i<SpansTemp.size(); i++){
				    		if(Long.parseLong(SmsApplicationLevelData.contactsList.get(position).content_uri_id) == SpansTemp.get(i).entityId){
				    			for(int j = 0; j< nativeGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< nativeChildDataTemp.get(j).size(); k++){
				    					if(Long.parseLong((String)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID)) == SpansTemp.get(i).entityId){
				    						nativeChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)nativeChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					nativeGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			for(int j = 0; j< privateGroupDataTemp.size(); j++){
				    				int noOfChecks = 0;
				    				for(int k = 0; k< privateChildDataTemp.get(j).size(); k++){
				    					if(Long.parseLong((String)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CONTACT_ID)) == SpansTemp.get(i).entityId){
				    						privateChildDataTemp.get(j).get(k).put(Constants.CHILD_CHECK, false);
				    					}
				    					if((Boolean)privateChildDataTemp.get(j).get(k).get(Constants.CHILD_CHECK)){
				    						noOfChecks = 1;
				    					}
				    				}
				    				if(noOfChecks>0){
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, true);
				    				}else{
				    					privateGroupDataTemp.get(j).put(Constants.GROUP_CHECK, false);
				    				}
				    			}
				    			SpansTemp.remove(i);
				    			
				    			nativeGroupAdapter.notifyDataSetChanged();
				    			privateGroupAdapter.notifyDataSetChanged();
				    		}
				    	}
					}
				}
			});
    		
    		return convertView;
		}
	}
	//************************************************************** end of ContactsAdapter******************
	

	
	public void nativeGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		nativeGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	nativeGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	nativeChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return nativeChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += nativeChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return nativeChildDataTemp.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return nativeGroupDataTemp.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		@Override
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.group_expl_list_group_row_design, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));
    			
    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									nativeAddCheck(groupPosition, i);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}else{
							nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									nativeRemoveCheck(groupPosition, i);
								}
							}
							nativeGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isExpanded){
							nativeGroupExplList.collapseGroup(groupPosition);
						}else{
							nativeGroupExplList.expandGroup(groupPosition);
						}
					}
				});
    			return convertView;
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				final ChildListHolder holder;
				if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row_design, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
    			holder.childNameText.setText((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME));
    			holder.childNumberText.setText((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
    			holder.childContactImage.setImageBitmap((Bitmap)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			holder.childCheck.setChecked((Boolean)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK));
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							nativeAddCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							nativeRemoveCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							holder.childCheck.setChecked(false);
							nativeRemoveCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllDeselected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(true);
							nativeAddCheck(groupPosition, childPosition);
							contactsAdapter.notifyDataSetChanged();
							boolean areAllSelected = true;
							for(int i = 0; i< nativeChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) nativeChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								nativeGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								nativeGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			
			@Override
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			@Override
			public boolean hasStableIds() {
			   return false;
			}
			 
			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    		
    	};
    }
	
	
	
	
	
	public void privateGroupsAdapterSetup(){
		
		final LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		privateGroupAdapter = new SimpleExpandableListAdapter(
    	    	this,
    	    	privateGroupDataTemp,
    	    	android.R.layout.simple_expandable_list_item_1,
    	    	new String[]{ Constants.GROUP_NAME },
    	    	new int[] { android.R.id.text1 },
    	    	privateChildDataTemp,
    	    	0,
    	    	null,
    	    	new int[] {}
    	){
			
			@Override
			public Object getChild(int groupPosition, int childPosition) {
			   return privateChildDataTemp.get(groupPosition).get(childPosition);
			}
			
			
			@Override
			public long getChildId(int groupPosition, int childPosition) {
			   long id = childPosition;
			   for(int i = 0; i<groupPosition; i ++) {
				   id += privateChildDataTemp.get(groupPosition).size(); 
			   }
			   return id;
			}
    		
    		
    		@Override
    		public int getChildrenCount(int groupPosition) {
    		   return privateChildDataTemp.get(groupPosition).size();
    		}
    		 
    		@Override
    		public Object getGroup(int groupPosition) {
    		   return privateChildDataTemp.get(groupPosition);
    		}
    		 
    		@Override
    		public int getGroupCount() {
    		   return privateGroupDataTemp.size();
    		}
    		 
    		@Override
    		public long getGroupId(int groupPosition) {
    		   return groupPosition;
    		}
    		
    		
    		@Override
			public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
    			final GroupListHolder holder;
    			if(convertView == null) {
    				LayoutInflater li = getLayoutInflater();
        			convertView = li.inflate(R.layout.group_expl_list_group_row_design, null);
        			holder = new GroupListHolder();
        			holder.groupHeading 	= (TextView) convertView.findViewById(R.id.group_expl_list_group_row_group_name);
        			holder.groupCheck		= (CheckBox) convertView.findViewById(R.id.group_expl_list_group_row_group_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (GroupListHolder) convertView.getTag();
    			}
    			holder.groupHeading.setText((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_NAME));
    			holder.groupCheck.setChecked((Boolean)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_CHECK));

    			holder.groupCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.groupCheck.isChecked()){
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									privateAddCheck(groupPosition, i);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}else{
							privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean)privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									privateRemoveCheck(groupPosition, i);
								}
							}
							privateGroupAdapter.notifyDataSetChanged();
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isExpanded){
							privateGroupExplList.collapseGroup(groupPosition);
						}else{
							privateGroupExplList.expandGroup(groupPosition);
						}
						
					}
				});
    			
    			return convertView;
    			
    		}



			@Override
    		public android.view.View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, android.view.View convertView, android.view.ViewGroup parent) {
    			
				privateChildDataTemp.get(groupPosition).get(childPosition);
				final ChildListHolder holder;
    			if(convertView == null){
    				convertView = layoutInflater.inflate(R.layout.contacts_list_row_design, null, false);
    				holder = new ChildListHolder();
    				holder.childNameText  		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_name);
        			holder.childContactImage 	= (ImageView) convertView.findViewById(R.id.contact_list_row_contact_pic);
        			holder.childNumberText		= (TextView)  convertView.findViewById(R.id.contact_list_row_contact_number);
        			holder.childCheck			= (CheckBox)  convertView.findViewById(R.id.contact_list_row_contact_check);
        			convertView.setTag(holder);
    			}else{
    				holder = (ChildListHolder) convertView.getTag();
    			}
    			
    			holder.childNameText.setText((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME));
    			holder.childNumberText.setText((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NUMBER));
    			holder.childContactImage.setImageBitmap((Bitmap)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_IMAGE));
    			holder.childCheck.setChecked((Boolean)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CHECK));
    			
    			holder.childCheck.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(holder.childCheck.isChecked()){
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							privateAddCheck(groupPosition, childPosition);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
								
						}else{
							privateRemoveCheck(groupPosition, childPosition);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			
    			
    			
    			convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(!holder.childCheck.isChecked()){
							holder.childCheck.setChecked(true);
							//check the contact in contact list if not checked and create span, add group in group ids of the span if contact already checked////
							privateAddCheck(groupPosition, childPosition);
							boolean areAllSelected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if(!((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK))){
									areAllSelected = false;
									break;
								}
							}
							if(areAllSelected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, true);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}else{
							holder.childCheck.setChecked(false);
							privateRemoveCheck(groupPosition, childPosition);
							
							boolean areAllDeselected = true;
							for(int i = 0; i< privateChildDataTemp.get(groupPosition).size(); i++){
								if((Boolean) privateChildDataTemp.get(groupPosition).get(i).get(Constants.CHILD_CHECK)){
									areAllDeselected = false;
									break;
								}
							}
							if(areAllDeselected){
								privateGroupDataTemp.get(groupPosition).put(Constants.GROUP_CHECK, false);
								privateGroupAdapter.notifyDataSetChanged();
							}
						}
					}
				});
    			
    			return convertView;
    		}
			
			
			@Override
			public boolean areAllItemsEnabled()
			{
			    return true;
			}
			
			
			@Override
			public boolean hasStableIds() {
			   return false;
			}
			 
			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
			   return true;
			}
    	};
    }
	
	
	
	
	
	public void nativeAddCheck(int groupPosition, int childPosition){
		nativeChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, true);
		boolean spanExist = false;
		for(int i = 0; i < SpansTemp.size(); i++){
			if(SpansTemp.get(i).entityId == Long.parseLong((String) nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID))){
				spanExist = true;
				try{
					SpansTemp.get(i).groupIds.add(Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}catch (ClassCastException e) {
					SpansTemp.get(i).groupIds.add(((Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}
				SpansTemp.get(i).groupTypes.add(1);
				break;
			}
		}
		if(!spanExist){
			SpannedEntity span = new SpannedEntity(-1, 2, (String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME), Long.parseLong((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)), -1);
			try{
				span.groupIds.add(((Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}catch (ClassCastException e) {
				span.groupIds.add(Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}
			span.groupTypes.add(1);
			SpansTemp.add(span);
			contactsAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	
	public void nativeRemoveCheck(int groupPosition, int childPosition){
		nativeChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, false);
		for(int i = 0; i < SpansTemp.size(); i++){
			if(Long.parseLong((String)nativeChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID))==SpansTemp.get(i).entityId){
				for(int j = 0; j< SpansTemp.get(i).groupIds.size(); j++){
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)nativeGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID);
					}
					groupTypeToRemove = 1;
					
					if(SpansTemp.get(i).groupIds.get(j) == groupIdToRemove && SpansTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						SpansTemp.get(i).groupIds.remove(j);
						if(SpansTemp.get(i).groupIds.size()==0){
							SpansTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	
	public void privateAddCheck(int groupPosition, int childPosition){
		Log.d("entering childcheck is checked true listner");
		privateChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, true);
		boolean spanExist = false;
		for(int i = 0; i < SpansTemp.size(); i++){
			if(SpansTemp.get(i).entityId == Long.parseLong((String) privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID))){
				spanExist = true;
				try{
					SpansTemp.get(i).groupIds.add(Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}catch (ClassCastException e) {
					SpansTemp.get(i).groupIds.add(((Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
				}
					SpansTemp.get(i).groupTypes.add(2);
				break;
			}
		}
		if(!spanExist){
			SpannedEntity span = new SpannedEntity(-1, 2, (String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_NAME), Long.parseLong((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID)), -1);
			try{
				span.groupIds.add(((Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}catch (ClassCastException e) {
				span.groupIds.add(Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID)));
			}
			span.groupTypes.add(2);
			SpansTemp.add(span);
			contactsAdapter.notifyDataSetChanged();
		}
	}	
		
	
	
	
	public void privateRemoveCheck(int groupPosition, int childPosition){
		privateChildDataTemp.get(groupPosition).get(childPosition).put(Constants.CHILD_CHECK, false);
		for(int i = 0; i < SpansTemp.size(); i++){
			if(Long.parseLong((String)privateChildDataTemp.get(groupPosition).get(childPosition).get(Constants.CHILD_CONTACT_ID))==SpansTemp.get(i).entityId){
				for(int j = 0; j< SpansTemp.get(i).groupIds.size(); j++){
					Long groupIdToRemove;
					int groupTypeToRemove;
					try{
						groupIdToRemove = Long.parseLong((String)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID));
					}catch (ClassCastException e) {
						groupIdToRemove = (Long)privateGroupDataTemp.get(groupPosition).get(Constants.GROUP_ID);
					}
					groupTypeToRemove = 2;
					if(SpansTemp.get(i).groupIds.get(j) == groupIdToRemove && SpansTemp.get(i).groupTypes.get(j) == groupTypeToRemove){
						SpansTemp.get(i).groupIds.remove(j);
						if(SpansTemp.get(i).groupIds.size()==0){
							SpansTemp.remove(i);
							contactsAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}
	
	
	
	
	class RecentsAdapter extends ArrayAdapter {
		
		RecentsAdapter(){
    		super(ContactsTabsActivity.this, R.layout.contacts_list_row_design, recentIds);
    	}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final RecentsListHolder holder;
			final int _position  = position;
    		if(convertView == null){
    			LayoutInflater inflater = getLayoutInflater();
    			convertView = inflater.inflate(R.layout.contacts_list_row_design, parent, false);
    			holder = new RecentsListHolder();
    			holder.contactImage 		= (ImageView) 	convertView.findViewById(R.id.contact_list_row_contact_pic);
        		holder.nameText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_name);
        		holder.numberText 			= (TextView) 	convertView.findViewById(R.id.contact_list_row_contact_number);
        		holder.contactCheck 		= (CheckBox) 	convertView.findViewById(R.id.contact_list_row_contact_check);
    			convertView.setTag(holder);
    		}else{
    			holder = (RecentsListHolder) convertView.getTag();
    		}
    		
    		int i = 0;
    		
    		if(recentContactIds.get(position)> -1){
    			for(i = 0; i< SmsApplicationLevelData.contactsList.size(); i++){
    				if(Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id) == recentContactIds.get(position)){
    					holder.contactImage.setImageBitmap(SmsApplicationLevelData.contactsList.get(i).image);
    		    		holder.nameText.setText(SmsApplicationLevelData.contactsList.get(i).name);
    		    		holder.numberText.setText(SmsApplicationLevelData.contactsList.get(i).number);
    		    		
    		    		for(int j = 0; j< SpansTemp.size(); j++){
    		        		if(Long.parseLong(SmsApplicationLevelData.contactsList.get(i).content_uri_id) == SpansTemp.get(j).entityId){
    		        			holder.contactCheck.setChecked(true);
    		        			break;
    		        		}else{
    		        			holder.contactCheck.setChecked(false);
    		        		}
    		        	}
    		    		break;
    				}
    			}
    		}else if(recentContactIds.get(position) == -1){
    			holder.contactImage.setImageBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
    			holder.nameText.setText(recentContactNumbers.get(position));
    			holder.numberText.setText("");
    			for(int j = 0; j< SpansTemp.size(); j++){
    				if(SpansTemp.get(j).displayName.equals(recentContactNumbers.get(position))){
    					holder.contactCheck.setChecked(true);
    					break;
    				}else{
    					holder.contactCheck.setChecked(true);
    				}
    			}
    		}
    		convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!holder.contactCheck.isChecked()){
						holder.contactCheck.setChecked(true);
						SpannedEntity span = new SpannedEntity();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsApplicationLevelData.contactsList.size(); k++){
								if(Long.parseLong(SmsApplicationLevelData.contactsList.get(k).content_uri_id) == recentContactIds.get(_position)){
									span = new SpannedEntity(-1, 2, SmsApplicationLevelData.contactsList.get(k).name, Long.parseLong(SmsApplicationLevelData.contactsList.get(k).content_uri_id), -1);
									break;
								}
							}
						}else{
							for(int k = 0; k< SmsApplicationLevelData.contactsList.size(); k++){
								if(SmsApplicationLevelData.contactsList.get(k).name.equals(recentContactNumbers.get(_position))){
									
								}
							}
							span = new SpannedEntity(-1, 1, recentContactNumbers.get(_position), -1, -1);
						}
						span.groupIds.add((long) -1);
						span.groupTypes.add(-1);
						SpansTemp.add(span);
						contactsAdapter.notifyDataSetChanged();
					}else{
						holder.contactCheck.setChecked(false);
						for(int i = 0; i< SpansTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == SpansTemp.get(i).entityId){
				    				SpansTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(SpansTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				SpansTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		
    		holder.contactCheck.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(holder.contactCheck.isChecked()){
						SpannedEntity span = new SpannedEntity();
						if(recentContactIds.get(_position)> -1){
							for(int k = 0; k< SmsApplicationLevelData.contactsList.size(); k++){
								if(Long.parseLong(SmsApplicationLevelData.contactsList.get(k).content_uri_id) == recentContactIds.get(_position)){
									span = new SpannedEntity(-1, 2, SmsApplicationLevelData.contactsList.get(k).name, Long.parseLong(SmsApplicationLevelData.contactsList.get(k).content_uri_id), -1);
									break;
								}
							}
						}else{
							span = new SpannedEntity(-1, 1, recentContactNumbers.get(_position), -1, -1);
						}
						span.groupIds.add((long) -1);
						span.groupTypes.add(-1);
						
						SpansTemp.add(span);
						
						contactsAdapter.notifyDataSetChanged();
					}else{
						for(int i = 0; i< SpansTemp.size(); i++){
				    		if(recentContactIds.get(_position)>-1){
				    			if(recentContactIds.get(_position) == SpansTemp.get(i).entityId){
				    				SpansTemp.remove(i);
					    			contactsAdapter.notifyDataSetChanged();
					    			break;
					    		}
				    		}else{
				    			if(SpansTemp.get(i).displayName.equals(recentContactNumbers.get(_position))){
				    				SpansTemp.remove(i);
				    				contactsAdapter.notifyDataSetChanged();
				    				break;
				    			}
				    		}
				    	}
					}
				}
			});
    		
    		
    		return convertView;
		}
	}
	
	
	
	class ContactsListHolder{
		ImageView 	contactImage;
		TextView 	nameText;
		TextView 	numberText;
		CheckBox contactCheck;
	}
	
	
	class GroupListHolder{
		TextView groupHeading;
		CheckBox groupCheck;
	}
	
	
	class ChildListHolder{
		TextView childNameText;
		ImageView childContactImage;
		TextView childNumberText;
		CheckBox childCheck;
	}
	
	
	class RecentsListHolder{
		ImageView 	contactImage;
		TextView 	nameText;
		TextView 	numberText;
		CheckBox 	contactCheck;
	}
	
	
	
	
	public void reloadPrivateGroupData(){
		mdba.open();
		
		privateChildDataTemp.clear();
		privateGroupDataTemp.clear();
		
		if(origin.equals("new")){
			NewScheduleActivity.privateGroupData.clear();
			NewScheduleActivity.privateChildData.clear();
		}else{
			EditScheduledSmsActivity.privateGroupData.clear();
			EditScheduledSmsActivity.privateChildData.clear();
		}
        Cursor groupsCursor = mdba.fetchAllGroups();
        if(groupsCursor.moveToFirst()){
        	do{
        		HashMap<String, Object> group = new HashMap<String, Object>();
        		ArrayList<Long> spanIdsForGroup = mdba.fetchSpansForGroup(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)), 2);
        		group.put(Constants.GROUP_NAME, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_NAME)));
        		group.put(Constants.GROUP_IMAGE, new BitmapFactory().decodeResource(getResources(), R.drawable.expander_ic_maximized));
        		if(spanIdsForGroup.size()==0){
       				group.put(Constants.GROUP_CHECK, false);
       			}else{
       				for(int i = 0; i< SpansTemp.size(); i++){
       					for(int j = 0; j< spanIdsForGroup.size(); j++){
       						if(spanIdsForGroup.get(j)==SpansTemp.get(i).spanId){
       							group.put(Constants.GROUP_CHECK, true);
       							break;
       						}
       					}
       				}
       			}
        		group.put(Constants.GROUP_TYPE, 2);
        		group.put(Constants.GROUP_ID, groupsCursor.getString(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		if(origin.equals("new")){
        			NewScheduleActivity.privateGroupData.add(group);
        		}else{
        			EditScheduledSmsActivity.privateGroupData.add(group);
        		}
        		
        	
        		ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
        		ArrayList<Long> contactIds = mdba.fetchIdsForGroups(groupsCursor.getLong(groupsCursor.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
        		
        		for(int i = 0; i< contactIds.size(); i++){
        			for(int j = 0; j< SmsApplicationLevelData.contactsList.size(); j++){
        				if(contactIds.get(i)==Long.parseLong(SmsApplicationLevelData.contactsList.get(j).content_uri_id)){
        					HashMap<String, Object> childParameters = new HashMap<String, Object>();
        					childParameters.put(Constants.CHILD_NAME, SmsApplicationLevelData.contactsList.get(j).name);
        					childParameters.put(Constants.CHILD_NUMBER, SmsApplicationLevelData.contactsList.get(j).number);
        					childParameters.put(Constants.CHILD_CONTACT_ID, SmsApplicationLevelData.contactsList.get(j).content_uri_id);
        					childParameters.put(Constants.CHILD_IMAGE, SmsApplicationLevelData.contactsList.get(j).image);
        					childParameters.put(Constants.CHILD_CHECK, false);
        					for(int k = 0; k< spanIdsForGroup.size(); k++){
       							for(int m = 0; m< SpansTemp.size(); m++){
       								if(SpansTemp.get(m).spanId == spanIdsForGroup.get(k) && SpansTemp.get(m).entityId == contactIds.get(i)){
       									childParameters.put(Constants.CHILD_CHECK, true);
       								}
       							}
       						}

        					
        					child.add(childParameters);
        				}
        			}
        		}
        		if(origin.equals("new")){
        			NewScheduleActivity.privateChildData.add(child);
        		}else{
        			EditScheduledSmsActivity.privateChildData.add(child);
        		}
        	}while(groupsCursor.moveToNext());
        }
        
        mdba.close();
        
        if(origin.equals("new")){
        	for(int groupCount = 0; groupCount< NewScheduleActivity.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, NewScheduleActivity.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				for(int i = 0; i< SpansTemp.size(); i++){
        			for(int j = 0; j< SpansTemp.get(i).groupIds.size(); j++){
        				if((SpansTemp.get(i).groupIds.get(j)==group.get(Constants.GROUP_ID)) && SpansTemp.get(i).groupTypes.get(j) == 2){
        					group.put(Constants.GROUP_CHECK, true);
        					break;
        				}
        			}
        		}
				
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				for(int childCount = 0; childCount< NewScheduleActivity.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, NewScheduleActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));

					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}else{
			
			for(int groupCount = 0; groupCount< EditScheduledSmsActivity.privateGroupData.size(); groupCount++){
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put(Constants.GROUP_ID, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_ID));
				group.put(Constants.GROUP_NAME, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_NAME));
				group.put(Constants.GROUP_IMAGE, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_IMAGE));
				group.put(Constants.GROUP_TYPE, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_TYPE));
				group.put(Constants.GROUP_CHECK, EditScheduledSmsActivity.privateGroupData.get(groupCount).get(Constants.GROUP_CHECK));
				
				privateGroupDataTemp.add(group);
				ArrayList<HashMap<String, Object>> child = new ArrayList<HashMap<String, Object>>();
				Log.d(EditScheduledSmsActivity.privateChildData.get(groupCount).size()+"child data size from edit activity");
				for(int childCount = 0; childCount < EditScheduledSmsActivity.privateChildData.get(groupCount).size(); childCount++){
					
					HashMap<String, Object> childParams = new HashMap<String, Object>();
					childParams.put(Constants.CHILD_CONTACT_ID, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CONTACT_ID));
					childParams.put(Constants.CHILD_NAME, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NAME));
					childParams.put(Constants.CHILD_NUMBER, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_NUMBER));
					childParams.put(Constants.CHILD_IMAGE, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_IMAGE));
					childParams.put(Constants.CHILD_CHECK, EditScheduledSmsActivity.privateChildData.get(groupCount).get(childCount).get(Constants.CHILD_CHECK));
					child.add(childParams);
				}
				privateChildDataTemp.add(child);
			}
		}
	}
}