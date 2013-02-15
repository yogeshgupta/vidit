package com.vidit;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;

import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.*;

import com.facebook.*;
import com.vidit.R;

public class MainActivity extends FacebookActivity implements OnDataPass{
	
	private Fragment loginFragment;
	private boolean isResumed=false;
	private Session session;
	private MenuItem logInOut;
	private SessionState sessionState;
	private ArrayList<String> fragData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AlertDialog startAlert= new AlertDialog.Builder(this)
	      .setMessage("This is a data extensive app, if you are downloading or playing a video" +
	      		" we suggest you to connect to a wireless network to avoid exhausting your data plan.")
	      .setTitle("Vidit Message")
	      .setCancelable(true)
	      .setNeutralButton(android.R.string.ok,
	         new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton){}
	         })
	      .show();
		TextView alertTextMsg = (TextView) startAlert.findViewById(android.R.id.message);
		alertTextMsg.setTextSize(12);
		
		if(isOnline())
		{
			if(savedInstanceState==null)
			{
				loginFragment=new LogFragment();
				getSupportFragmentManager().beginTransaction()
				.add(android.R.id.content, loginFragment).commit();
				setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
			}
			else
			{
				loginFragment=(LogFragment)getSupportFragmentManager()
						.findFragmentById(android.R.id.content);
			}
		}
		else
		{
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("No internet connection found");
		}
			
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		logInOut=(MenuItem)findViewById(R.id.log_out);
		/*if(this.session==null)
			logInOut.setVisible(false);
		else
			logInOut.setVisible(true);*/
		getMenuInflater().inflate(R.menu.vidit_menu, menu);
		try
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
		        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		        SearchView searchView = (SearchView) menu.findItem(R.id.searche).getActionView();
		        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() 
		        {
		            @Override
		            public boolean onQueryTextSubmit(String query) {
		              startSearch(query);
		              return true;
		            }

		            @Override
		            public boolean onQueryTextChange(final String s) {
		              return false;
		            }
		         });
		        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		        searchView.setIconifiedByDefault(false);
		       
		    }
		}
		catch(Exception e)
		{
			Log.e("Vidit_TAG","I got an error",e);
		}
		
		return true;
	}
	

	  private void startSearch(final String query) 
	  {
	    // Doesn't call through onSearchRequest
	    Intent intent = new Intent(this, SearchResult.class);
	    intent.putExtra("Query", query);
	    Bundle bundle=new Bundle();
		bundle.putStringArrayList("extra", fragData);
		intent.putExtras(bundle);
	    startActivity(intent);
	  }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		
        switch (item.getItemId())
        {
        	case R.id.log_out:
        		this.session.closeAndClearTokenInformation();
        		return true;
        		
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
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    isResumed = true;
	}

	@Override
	public void onPause() 
	{
	    super.onPause();
	    isResumed = false;
	}
	
	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) 
	{
	    sessionState=state;
		super.onSessionStateChange(state, exception);
	    if (isResumed) {
	    	
	    	((LogFragment) loginFragment).onSessionStateChange(state, exception,this.getSession());
	    }
	}
	
	@Override
	protected void onResumeFragments() 
	{
	    super.onResumeFragments();

	    session = Session.getActiveSession();
	    if (session != null && (session.isOpened() || session.isClosed()) ) 
	    {
	        onSessionStateChange(session.getState(), null);
	    }
	}
	
	@Override
    public boolean onSearchRequested() {
    	Bundle bundle=new Bundle();
		bundle.putStringArrayList("extra", fragData);
		// search initial query
		startSearch(null, false, bundle, false);
		return true;
    }
	
	@Override
	public void onDataPass(ArrayList<String> data) 
	{
	    fragData=data;
	}
	
	/*@Override
	protected void onStop()
	{
		super.onStop();
		File storagePath = new File(Environment.getExternalStorageDirectory().toString() + "/FidVids");
		boolean delDirectory=deleteDirectory(storagePath);
    }
	
	
	//Delete the directory where images are saved when the app is exited
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      if (files == null) {
	          return true;
	      }
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }*/
	
	public boolean isOnline() 
	{
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}


