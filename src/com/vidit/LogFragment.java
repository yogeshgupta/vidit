package com.vidit;

import java.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.text.*;
import org.json.*;

import android.net.Uri;
import android.os.AsyncTask;
import android.annotation.SuppressLint;
import android.content.Intent;

//import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.facebook.*;
import com.facebook.android.Util;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.vidit.R;

public class LogFragment extends Fragment 
{
	
	protected static final String TAG = null;
	public int index=2;
	private LoginButton loginButton;
	private Button btnMyVideos;
	private Button btnFriendsVideos;
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private ListView listView;
	private JSONArray data1;
	private List<HashMap<String,String>> vidDetList;
		
	private ArrayList<String> imageUrl=new ArrayList<String>();
	private String firstName;
	private String lastName;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    View view = inflater.inflate(R.layout.main, container, false);
	    
	    List<String> lsr=new ArrayList<String>();
	    lsr.add("user_likes");
	    lsr.add("user_status");
	    lsr.add("user_videos");
	    lsr.add("friends_videos");
	    lsr.add("read_friendlists");
	    
	    //Assigning controls
	    
	    loginButton = (LoginButton) view.findViewById(R.id.loginButton);
	    loginButton.setReadPermissions(lsr);
	    loginButton.setApplicationId(getString(R.string.app_id));
	    profilePictureView = (ProfilePictureView) view.findViewById(R.id.fbPic);
	    profilePictureView.setCropped(true);
	    btnMyVideos=(Button)view.findViewById(R.id.btnVideoByMe);
	    btnFriendsVideos=(Button)view.findViewById(R.id.btnVideoByFriends);
	    userNameView = (TextView) view.findViewById(R.id.headLine);
	    listView = ( ListView ) view.findViewById(R.id.lstViewVideo);
	    	    
	    return view;
	}
	
	//Displaying user info
	public void onSessionStateChange(SessionState state, Exception exception,final Session session) 
	{
		try
		{
			if (state.isOpened()) /*Checking if already logged in */
			{
				loginButton.setVisibility(View.GONE);
				Request request = Request.newMeRequest(session, new Request.GraphUserCallback() 
				{
					@Override
					public void onCompleted(GraphUser user, Response response) 
					{
						if (user != null) 
						{
							profilePictureView.setProfileId(user.getId());
							// Set the Textview's text to the user's name
							userNameView.setText(user.getName());
						}
					}
				});
				Request.executeBatchAsync(request);
				

				
				//Displaying list in case of default view
				showList(index, session);
				
				//Onclick of My Videos button
				btnMyVideos.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) 
					{
						btnFriendsVideos.setText("Video By Friends");
						SpannableString content = new SpannableString("My Videos");
						content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
						btnMyVideos.setText(content);
						if(index!=1)
						{
							try
							{
								index=1;
								showList(index, session);
							}
							catch(Exception e)
							{
								Log.e("Your_APP_LOG_TAG","I got an error",e);
							}
						}
					}
				});
				
				btnFriendsVideos.setOnClickListener(new OnClickListener() 
				{
					
					@Override
					public void onClick(View arg0) 
					{
						btnMyVideos.setText("My Videos");
						SpannableString content = new SpannableString("Video By Friends");
						content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
						btnFriendsVideos.setText(content);
						if(index!=2)
						{
							try
							{
								index=2;
								showList(index, session);
							}
							catch(Exception e)
							{
								Log.e("Your_APP_LOG_TAG","I got an error",e);
							}
						}
					}
				});
			}
			else if (state.isClosed()) 
			{
				loginButton.setVisibility(View.VISIBLE);
				profilePictureView.setProfileId(null);
				userNameView.setText(null);
			}
		
		}
		catch(Exception e)
		{
			Log.e("Your_APP_LOG_TAG","I got an error",e);
		}
		
	}
	
	//To display the list of videos by category
	public void showList(final int no, Session session)
	{
		final ArrayList<String> ownerList=new ArrayList<String>();
		listView.setAdapter(null);
		data1=null;
		vidDetList=null;
		String fqlQuery = "{'myVideos':'SELECT vid, src, owner, title, description,thumbnail_link, created_time,length FROM video WHERE owner=me()'," +
        		"'friends':'SELECT uid2 FROM friend WHERE uid1 = me()',"+
        		"'friendsVideo':'SELECT vid, src, owner, title, description,thumbnail_link, created_time,length FROM video WHERE owner IN "+
        		"(SELECT uid2 FROM #friends) ORDER BY created_time'," +
        		"'ownerName':'SELECT uid,first_name,last_name FROM user WHERE uid IN " +
        		"(SELECT owner FROM #friendsVideo)',}";
        
		Bundle params = new Bundle();
        
		params.putString("q", fqlQuery);
        
		Request request1 = new Request(session,"/fql",params,HttpMethod.GET,new Request.Callback()
		{
			@SuppressLint("SimpleDateFormat")
			public void onCompleted(Response response)
			{
				int indexex=nthOccurrence(response.getGraphObject().toString(),'{',1);
				int index=response.getGraphObject().toString().length()-1;
				String edit=response.getGraphObject().toString().substring(indexex, index);
				
				try
				{
					JSONObject json = Util.parseJson(edit);
					JSONArray data = json.getJSONArray( "data" );
					JSONObject getVideo = data.getJSONObject(no);
					JSONObject getOwner = data.getJSONObject(3);
					String s="{data:"+getVideo.getString("fql_result_set")+"}";
					String s1="{data:"+getOwner.getString("fql_result_set")+"}";
					JSONObject json1 = Util.parseJson(s);
					JSONObject jsonOwner = Util.parseJson(s1);
					data1 = json1.getJSONArray("data");
					JSONArray ownerArray=jsonOwner.getJSONArray("data");
					vidDetList = new ArrayList<HashMap<String,String>>();
					for ( int i = 0, size = data1.length(); i < size; i++ )
					{
						HashMap<String, String> hm = new HashMap<String,String>();
						JSONObject getVidDetails=data1.getJSONObject(i);
						String title=getVidDetails.getString("title");
						if(title.equalsIgnoreCase(""))
						title="Untitled";
						hm.put("title", "Title : " + title );
						DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						hm.put("creationDate","Created On : " + formatter.format(Long.valueOf(getVidDetails.getString("created_time")).longValue()*1000));
						imageUrl.add(getVidDetails.getString("thumbnail_link"));
						try
						{
							String arr[]=new String[2];
							arr[0]=getVidDetails.getString("thumbnail_link");
							arr[1]=getVidDetails.getString("vid");
							new DownloadImageTask().execute(arr);
							
						}
						catch(Exception e)
						{
							Log.e("Your_APP_LOG_TAG","I got an error",e);
						}
						//new RetriveImages().execute(url);
						//saveImage(url, i);
						try
						{
							String pathName=Environment.getExternalStorageDirectory().toString()+"/FidVids/"+getVidDetails.getString("vid")+".jpg";
							hm.put("videoThumb",pathName);
						}
						catch(Exception e)
						{
							Log.e("Your_APP_LOG_TAG","I got an error",e);
						}
						for ( int j = 0, size1 = ownerArray.length(); j < size1; j++ )
						{
							JSONObject ownerObject=ownerArray.getJSONObject(j);
							if(getVidDetails.getString("owner").equalsIgnoreCase(ownerObject.getString("uid")))
							{
								firstName=ownerObject.getString("first_name");
								lastName=ownerObject.getString("last_name");
								break;
							}
							
						}
						if(no==1)
						{
							firstName="You";
							lastName="";
						}
						ownerList.add(firstName+" "+lastName);
						hm.put("owner","Owner : " +firstName+" "+lastName );
						vidDetList.add(hm);
					}
					String[] itemControl = {"videoThumb","title","creationDate","owner"};
					int[] itemLayout={R.id.videoThumb,R.id.title,R.id.creationDate,R.id.owner};
					SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), vidDetList, R.layout.listvideos_layout, itemControl, itemLayout);
					listView.setAdapter(adapter);
					
					getActivity().sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_MOUNTED,
							            Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			    	
					
					listView.setOnItemClickListener(new OnItemClickListener() 
					{
						
						@SuppressWarnings("rawtypes")
						public void onItemClick(AdapterView parent, View v, int position, long id)
						 {
							 try
							 {
								 JSONObject videoDetails=data1.getJSONObject(position);
								 Intent i = new Intent(getActivity(), VideoDetails.class);
								 i.putExtra("video_Details",videoDetails.toString());
								 i.putExtra("ownerDetails",ownerList.get(position));
								 i.putExtra("video_Thumb", Environment.getExternalStorageDirectory().toString()+"/FidVids/"+videoDetails.getString("vid")+".jpg");
								 startActivity(i);
							 }
							 catch(Exception e)
							 {
								 Log.e("Your_APP_LOG_TAG","I got an error",e);
							 }
						 }
					});
						
				}
				catch(Exception e)
				{
					Log.e("Your_APP_LOG_TAG","I got an error",e);
				}	
			}
		});
		Request.executeBatchAsync(request1);
			
	}	
	
	public static int nthOccurrence(String str, char c, int n) 
	{
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Void> 
	{
	    protected Void doInBackground(String... urls) 
	    {
	    	try
	    	{
	    		URL url = new URL (urls[0]);
	    		InputStream input = url.openStream();
	    	
	    		try {
	    			//The sdcard directory e.g. '/sdcard' can be used directly, or 
	    			//more safely abstracted with getExternalStorageDirectory()
	    			String root = Environment.getExternalStorageDirectory().toString();
	    			File storagePath = new File(root + "/FidVids");    
	    			storagePath.mkdirs();
	    			OutputStream output = new FileOutputStream (new File(storagePath,urls[1]+".jpg"));
	    			try {
	    				byte[] buffer = new byte[1024];
	    				int bytesRead = 0;
	    				while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
	    					output.write(buffer, 0, bytesRead);
	    				}
	    	    	}
	    			catch(Exception e)
	    			{
	    				Log.e("Your_APP_LOG_TAG","I got an error",e);
	    			}
	    			finally 
	    			{
	    				output.close();
	    			}
	    		}
	    		catch(Exception e)
	    		{
	    			Log.e("Your_APP_LOG_TAG","I got an error",e);
	    		}
	    		finally 
	    		{
	    			input.close();
	    		}
	    		
	    	}
	    	catch(Exception e)
	    	{
	    		Log.e("Your_APP_LOG_TAG","I got an error",e);
	    	}
			return null;
	    }
	}
	
}

	
	

