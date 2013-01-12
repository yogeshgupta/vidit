package com.vidit;

import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import com.vidit.R;

public class SearchResult extends Activity {
	
	private TextView searchString;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		String query="";
		
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_search_result);
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
		searchString=(TextView)findViewById(R.id.search);
		searchString.setText(query);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vidit_menu, menu);
		return true;
	}

}
