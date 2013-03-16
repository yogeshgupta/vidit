package com.vidit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.VideoView;
import com.vidit.R;

@SuppressLint("SimpleDateFormat")
public class VideoDetails extends Activity {
	
	private TextView tvTitle,tvDateTime,tvOwner,tvLength;
	private ImageView imgThumbnail;
	private Button btnPlayLQ,btnDownloadLQ,btnPlayHQ,btnDownloadHQ;
	private JSONObject jsonObj;
	private String title,vid;
	private VideoView fbVideo;
	private Context context;
	DownloadVideoTask dvTask=new DownloadVideoTask();
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_details_layout);
		try 
		{
			jsonObj = new JSONObject(getIntent().getStringExtra("video_Details"));
		} 
		catch (JSONException e) 
		{
			Log.e("Vidit_TAG","I got an error",e);
		}
		String strThumbLink=getIntent().getStringExtra("video_Thumb");
		context=this;
		tvTitle=(TextView)findViewById(R.id.tvTitle);
		tvDateTime=(TextView)findViewById(R.id.tvDateTime);
		tvLength=(TextView)findViewById(R.id.tvLength);
		tvOwner=(TextView)findViewById(R.id.tvOwner);
		imgThumbnail=(ImageView)findViewById(R.id.imgVideoThumbnail);
		btnDownloadLQ=(Button)findViewById(R.id.btnDownloadLQ);
		btnPlayLQ=(Button)findViewById(R.id.btnPlayLQ);
		btnDownloadHQ=(Button)findViewById(R.id.btnDownloadHQ);
		btnPlayHQ=(Button)findViewById(R.id.btnPlayHQ);
		fbVideo=(VideoView)findViewById(R.id.vvPlay);
		//jsonArrayList=new ArrayList<String>();
		//jsonArrayList=getIntent().getStringArrayListExtra("passJson");
		try
		{
			title=jsonObj.getString("title");
			vid=jsonObj.getString("vid");
			if(title.equalsIgnoreCase(""))
				title="Untitled";
			tvTitle.setText("Title: "+title);
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			tvDateTime.setText("Added On: " + formatter.format(Long.valueOf(jsonObj.getString("created_time")).longValue()*1000));
			float length=Float.parseFloat(jsonObj.getString("length"));
			String prefix=" Seconds";
			if(length>60)
			{
				length/=60;
				prefix=" Minutes";
			}
			tvLength.setText("Length: "+length+prefix);
			tvOwner.setText("Owner: "+getIntent().getStringExtra("ownerDetails"));
			Bitmap bmp = BitmapFactory.decodeFile(strThumbLink);
			imgThumbnail.setImageBitmap(bmp);
			
			setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
			mProgressDialog = new ProgressDialog(VideoDetails.this);
			mProgressDialog.setMessage("Downloading "+title);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			        dvTask.cancel(true);
			    }
			});
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		catch(Exception e)
		{
			Log.e("Vidit_TAG","I got an error",e);
		}
		
		
		
		//To Download low quality videos
		btnDownloadLQ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				try
				{
					if(isDownloadManagerAvailable(context))
					{
						String url = jsonObj.getString("src");
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.replace("https://", "http://")));
						request.setDescription("Downloading "+title);
						request.setTitle(title+"_lq.mp4");
						// in order for this if to run, you must use the android 3.2 to compile your app
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						    request.allowScanningByMediaScanner();
						    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						}
						
						request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title+".mp4");
						
						// get download service and enqueue file
						DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
						manager.enqueue(request);
					}
					else
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							dvTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObj.getString("src"));
							else
							dvTask.execute(jsonObj.getString("src"));
					}
					
				}
				catch(Exception e)
				{
					Log.e("Vidit_TAG","I got an error",e);
				}
			}
		});
		
		//To Download high quality videos
		btnDownloadHQ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				try
				{
					if(isDownloadManagerAvailable(context))
					{
						String url = jsonObj.getString("src_hq");
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.replace("https://", "http://")));
						request.setDescription("Downloading "+title);
						request.setTitle(title+"_hq.mp4");
						// in order for this if to run, you must use the android 3.2 to compile your app
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						    request.allowScanningByMediaScanner();
						    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
						}
						request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title+".mp4");

						// get download service and enqueue file
						DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
						manager.enqueue(request);
					}
					else
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							dvTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObj.getString("src_hq"));
							else
							dvTask.execute(jsonObj.getString("src_hq"));
					}
					
				}
				catch(Exception e)
				{
					Log.e("Vidit_TAG","I got an error",e);
				}
			}
		});
		
		//To play low quality videos
		btnPlayLQ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				try
				{
					Uri uri=Uri.parse(jsonObj.getString("src"));
					fbVideo.setBackgroundColor(Color.TRANSPARENT);
					fbVideo.setVideoURI(uri);
				    fbVideo.setMediaController(new MediaController(context));
				    fbVideo.requestFocus();
				    fbVideo.start();
					
				}
				catch(Exception e)
				{
					Log.e("Vidit_TAG","I got an error",e);
				}
			}
		});
		
		//To play high quality videos
		btnPlayHQ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				try
				{
					Uri uri=Uri.parse(jsonObj.getString("src_hq"));
					fbVideo.setBackgroundColor(Color.TRANSPARENT);
					fbVideo.setVideoURI(uri);
				    fbVideo.setMediaController(new MediaController(context));
				    fbVideo.requestFocus();
				    fbVideo.start();
					
				}
				catch(Exception e)
				{
					Log.e("Vidit_TAG","I got an error",e);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getMenuInflater().inflate(R.menu.vidit_menu2, menu);
		else
			getMenuInflater().inflate(R.menu.vidit_menugb2, menu);
		
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
	    bundle.putStringArrayList("extra", getIntent().getStringArrayListExtra("extra"));
		intent.putExtras(bundle);
	    startActivity(intent);
	  }
	
	//Video Download async method
	private class DownloadVideoTask extends AsyncTask<String, Integer, String> 
	{
		
		
		@Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mProgressDialog.show();
	    }
		
		 @Override
		    protected String doInBackground(String... sUrl) {
		        try {
		            URL url = new URL(sUrl[0]);
		            URLConnection connection = url.openConnection();
		            connection.connect();
		            // this will be useful so that you can show a typical 0-100% progress bar
		            int fileLength = connection.getContentLength();

		            // download the file
		            InputStream input = new BufferedInputStream(url.openStream());
		            String root = Environment.getExternalStorageDirectory().toString();
	    			File storagePath = new File(root + "/vidit");    
	    			storagePath.mkdirs();
	    			if(title=="Untitled")
	    				title+="_"+vid;
	    			OutputStream output = new FileOutputStream (new File(storagePath,title+".mp4"));

		            byte data[] = new byte[1024];
		            long total = 0;
		            int count;
		            while ((count = input.read(data)) != -1) {
		                total += count;
		                // publishing the progress....
		                publishProgress((int) (total * 100 / fileLength));
		                output.write(data, 0, count);
		            }

		            output.flush();
		            output.close();
		            input.close();
		        } catch (Exception e) {
		        }
		        return null;
		    }
	    
	    
	    
		@Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        mProgressDialog.setProgress(progress[0]);
	    }
	    
		
		@Override
		protected void onPostExecute(String unused)
		{
			mProgressDialog.dismiss();
			alertbox(title);
		}
	}
	
	protected void alertbox(String msgTitle)
	   {
	   new AlertDialog.Builder(this)
	      .setMessage(msgTitle+" has been successfully downloaded!")
	      .setTitle("Video Downloaded")
	      .setCancelable(true)
	      .setNeutralButton(android.R.string.ok,
	         new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton){}
	         })
	      .show();
	   }
	
/*	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                if(title=="Untitled")
    				title+="_"+vid;
                mProgressDialog.setMessage("Downloading "+title);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }*/
	
	@Override
	public boolean onSearchRequested() {
    	Bundle bundle1=new Bundle();
		bundle1.putStringArrayList("extra", getIntent().getStringArrayListExtra("extra"));
		// search initial query
		startSearch(null, false, bundle1, false);
		return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        	case R.id.home:
    		startActivity(new Intent(VideoDetails.this,MainActivity.class));
    		return true;
        		
        	case R.id.exit:
        		this.finish();
        		return true;
        		
        	case R.id.search:
        		onSearchRequested();
        		return true;
        		
        	case R.id.aboutVidit:
        		AlertDialog startAlert= new AlertDialog.Builder(this)
      	      .setMessage("Vidit is an open source app, developed in free time."
      	    		  +"Incase you want to contribute to it kindly visit the " +
      	    		  "github page or contact the developer. You can also support the " +
      	    		  "developer by making some donations.")
      	      .setTitle("About Vidit")
      	      .setCancelable(true)
      	      .setNeutralButton(android.R.string.ok,
      	         new DialogInterface.OnClickListener() {
      	         public void onClick(DialogInterface dialog, int whichButton){}
      	         })
      	      .show();
        		return true;
        		
        	default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
