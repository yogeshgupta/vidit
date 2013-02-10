package com.vidit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.vidit.R;

public class SearchResult extends Activity {
	
	
	private ListView listView;
	private TextView shwMsg;
	private List<HashMap<String,String>> vidDetList;
	private ArrayList<String> jsonArrayList;
	private int count;
	final ArrayList<String> ownerList=new ArrayList<String>();
	
	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		String query="";
		jsonArrayList=new ArrayList<String>();
		JSONArray getarray;
		count=0;
		shwMsg=(TextView)findViewById(R.id.shwMsg);
		listView = ( ListView ) findViewById(R.id.lstVideoSearch);
		shwMsg.setVisibility(View.GONE);
		try
		{
			
			Intent intent=getIntent();
			if(intent.ACTION_SEARCH.equals(intent.getAction()))
			{
				query=intent.getStringExtra(SearchManager.QUERY);
				
			}
			
		}
		catch(Exception e)
		{
			Log.e("Your_APP_LOG_TAG","I got an error",e);
		}
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		try 
		{
			Bundle bundle=getIntent().getBundleExtra(SearchManager.APP_DATA);
			jsonArrayList=bundle.getStringArrayList("extra");
			final JSONArray js=new JSONArray(jsonArrayList.get(0));
			JSONArray js1=new JSONArray(jsonArrayList.get(1));
			final HashMap<String, String> hm1 = new HashMap<String,String>();
			vidDetList = new ArrayList<HashMap<String,String>>();
			for ( int i = 0, size = js.length(); i < size; i++ )
			{
				JSONObject getVidDetails=js.getJSONObject(i);
				String title=getVidDetails.getString("title");
				if(title.equalsIgnoreCase(""))
				title="Untitled";
				if(searchString(title.toLowerCase(),query.toLowerCase()))
				{
					HashMap<String, String> hm = new HashMap<String,String>();
					hm.put("title", "Title : " + title );
					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					hm.put("creationDate","Created On : " + formatter.format(Long.valueOf(getVidDetails.getString("created_time")).longValue()*1000));
					String pathName=Environment.getExternalStorageDirectory().toString()+"/.FidVids/"+getVidDetails.getString("vid")+".jpg";
					hm.put("videoThumb",pathName);
					
					for ( int j = 0, size1 = js1.length(); j < size1; j++ )
					{
						JSONObject ownerObject=js1.getJSONObject(j);
						if(getVidDetails.getString("owner").equalsIgnoreCase(ownerObject.getString("uid")))
						{
							String firstName=ownerObject.getString("first_name");
							String lastName=ownerObject.getString("last_name");
							ownerList.add(firstName+" "+lastName);
							hm.put("owner","Owner : " +firstName+" "+lastName );
							break;
						}						
					}
					vidDetList.add(hm);
					hm1.put(Integer.toString(count),Integer.toString(i));
					count++;
				}
			}
			
			for ( int j = 0, size1 = js1.length(); j < size1; j++ )
			{
				JSONObject ownerObject=js1.getJSONObject(j);
				String name=ownerObject.getString("first_name")+" "+ownerObject.getString("last_name");
				if(searchString(name.toLowerCase(),query.toLowerCase()))
				{
					for ( int i = 0, size = js.length(); i < size; i++ )
					{
						JSONObject getVidDetails=js.getJSONObject(i);
						if(ownerObject.getString("uid").equalsIgnoreCase(getVidDetails.getString("owner")))
						{
							HashMap<String, String> hm = new HashMap<String,String>();
							String title=getVidDetails.getString("title");
							if(title.equalsIgnoreCase(""))
							title="Untitled";
							hm.put("title", "Title : " + title );
							DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
							hm.put("creationDate","Created On : " + formatter.format(Long.valueOf(getVidDetails.getString("created_time")).longValue()*1000));
							String pathName=Environment.getExternalStorageDirectory().toString()+"/.FidVids/"+getVidDetails.getString("vid")+".jpg";
							hm.put("videoThumb",pathName);
							ownerList.add(name);
							hm.put("owner","Owner : "+name);
							vidDetList.add(hm);
							hm1.put(Integer.toString(count),Integer.toString(i));
							count++;
						}
					}
				}
			}
			String[] itemControl = {"videoThumb","title","creationDate","owner"};
			int[] itemLayout={R.id.videoThumb,R.id.title,R.id.creationDate,R.id.owner};
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), vidDetList, R.layout.listvideos_layout, itemControl, itemLayout);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new OnItemClickListener() 
			{
					
				@SuppressWarnings("rawtypes")
				public void onItemClick(AdapterView parent, View v, int position, long id)
				{
					 try
					 {
						 JSONObject videoDetails=js.getJSONObject(Integer.parseInt(hm1.get(Integer.toString(position))));
						 Intent i = new Intent(SearchResult.this, VideoDetails.class);
						 i.putExtra("extra", jsonArrayList);
						 i.putExtra("video_Details",videoDetails.toString());
						 i.putExtra("ownerDetails",ownerList.get(position));
						 i.putExtra("video_Thumb", Environment.getExternalStorageDirectory().toString()+"/.FidVids/"+videoDetails.getString("vid")+".jpg");
						 startActivity(i);
					 }
					 catch(Exception e)
					 {
						 Log.e("Vidit_TAG","I got an error",e);
					 }
				 }
			});
		} 
		catch (Exception e) {
			Log.e("Your_APP_LOG_TAG","I got an error",e);
		}
		if(vidDetList.size()==0)
		{
			shwMsg.setVisibility(View.VISIBLE);
			shwMsg.setText("No Result Found");
		}
			
	}
	
	@Override
	public boolean onSearchRequested() {
    	Bundle bundle1=new Bundle();
		bundle1.putStringArrayList("extra", jsonArrayList);
		// search initial query
		startSearch(null, false, bundle1, false);
		return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vidit_menu, menu);
		return true;
	}
	
	public boolean searchString(String a, String b) 
	{
		if(a.indexOf(b)>=0)
			return true;
		else
			return false;

	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        	/*case R.id.log_out:
        		this.session.closeAndClearTokenInformation();
        		return true;*/
        		
        	case R.id.exit:
        		this.finish();
        		return true;
        		
        	case R.id.search:
        		onSearchRequested();
        		return true;
        		
        	default:
                return super.onOptionsItemSelected(item);
        }
    }

}
