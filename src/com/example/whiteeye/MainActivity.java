package com.example.whiteeye;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.View;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private ImageView display;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
       

        final String [] items			= new String [] {"Take from camera", "Select from gallery"};
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) { //pick from camera
				if (item == 0) {
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
									   "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

					try {
						intent.putExtra("return-data", true);

						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { //pick from file
					Intent intent = new Intent();

	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);

	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
	               
				}
			}
		} );

		final AlertDialog dialog = builder.create();

		Button button 	= (Button) findViewById(R.id.btn_crop);
		mImageView		= (ImageView) findViewById(R.id.iv_photo);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});
		
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;

	    switch (requestCode) {
		    case PICK_FROM_CAMERA:
		    	doCrop();
		    	
		    	break;

		    case PICK_FROM_FILE:
		    	mImageCaptureUri = data.getData();

		    	doCrop();

		    	break;

		    case CROP_FROM_CAMERA:
		        Bundle extras = data.getExtras();

		        if (extras != null) {
		            Bitmap photo = extras.getParcelable("data");

		            mImageView.setImageBitmap(photo);
		        }

		        File f = new File(mImageCaptureUri.getPath());

		        if (f.exists()) f.delete();
		        System.out.println("HI");
		        display();
		        //getPixel();
		        break;

	    }
	    
	}

    private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );

        int size = list.size();

        if (size == 0) {
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

            return;
        } else {
        	intent.setData(mImageCaptureUri);

            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

        	if (size == 1) {
        		Intent i 		= new Intent(intent);
	        	ResolveInfo res	= list.get(0);

	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

	        	startActivityForResult(i, CROP_FROM_CAMERA);
        	} else {
		        for (ResolveInfo res : list) {
		        	final CropOption co = new CropOption();

		        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
		        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
		        	co.appIntent= new Intent(intent);

		        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

		            cropOptions.add(co);
		        }

		        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Choose Crop App");
		        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		            public void onClick( DialogInterface dialog, int item ) {
		                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
		            }
		        });

		        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
		            @Override
		            public void onCancel( DialogInterface dialog ) {

		                if (mImageCaptureUri != null ) {
		                    getContentResolver().delete(mImageCaptureUri, null, null );
		                    mImageCaptureUri = null;
		                }
		            }
		        } );

		        AlertDialog alert = builder.create();

		        alert.show();
        	}
        }
        //System.out.println("IM ALIVE");
        
	}
    public void display() {
    	Resources res = getResources();
    	Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
    	
    	int xmin = bitmap.getWidth()/4,
    		xmax = xmin * 3,
    	    ymin = bitmap.getHeight()/4,
    	    ymax = ymin * 3;
    	long redTotal = 0;
    	long blueTotal = 0;
    	long greenTotal = 0;

    	// Sum up for average over center
    	for(int i = xmin; i <= xmax; i++){
    		for(int j = ymin; j <= ymax; j++){
    	    	int pixel = bitmap.getPixel(i, j);
    	    	
    	    	redTotal += Color.red(pixel);
    	    	blueTotal += Color.blue(pixel);
    	    	greenTotal += Color.green(pixel);
    		}
    	}    	
    	
    	// Divide for average
    	long redAvg =  (long) (redTotal / ((xmax-xmin) * (ymax-ymin)));
    	long blueAvg =  (long) (blueTotal / ((xmax-xmin) * (ymax-ymin)));
    	long greenAvg =  (long) (greenTotal / ((xmax-xmin) * (ymax-ymin)));
    	
    	// Correct for possible overflow 
    	if(redAvg > 255) redAvg = 255;
    	if(blueAvg > 255) blueAvg = 255;
    	if(greenAvg > 255) greenAvg = 255;
    	
    	Drawable drawable = res.getDrawable(R.drawable.test);
    	drawable.setColorFilter(Color.rgb((int)redAvg,(int)greenAvg,(int)blueAvg), PorterDuff.Mode.MULTIPLY);
        display = (ImageView) findViewById(R.id.test);
    	display.setImageDrawable(drawable);
    	
    	
    
    }
    
    /*public void getPixel(){
    	//mImageView.buildDrawingCache();
    	//Bitmap bmap = imageView.getDrawingCache();
    	//BitmapDrawable test = (BitmapDrawable)(mImageView.getDrawable());
    	Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
    	//System.out.println("STILL ALIVE");
    	int pixel = bitmap.getPixel(2,2);//ARBITRARY NUMBERS
    	int red = Color.red(pixel);
    	int blue = Color.blue(pixel);
    	int green = Color.green(pixel);
    	System.out.println("pixel "+pixel);
    	System.out.println("Red: "+red+" Blue: "+blue+" Green: "+green);
    	System.out.println("Red over White is: "+(red+0.0)/(red+green+blue));
    }*/
    

}