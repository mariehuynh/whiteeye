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
import android.text.method.LinkMovementMethod;
import android.text.Html;
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

		Button cropButton = (Button) findViewById(R.id.btn_crop);
		mImageView = (ImageView) findViewById(R.id.iv_photo);

		cropButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});

        Button infoButton = (Button) findViewById(R.id.btn_info);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.retinoblastoma.net/what_is.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
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
		            int red = 0xFFFF0000;

		            // Draw horizontal red lines around area scanned
		            for(int x = (int)(photo.getWidth()*.35); x<(int)(photo.getWidth()*.65); x++) {
		            	photo.setPixel(x, (int) (photo.getHeight()*.35), red);
		            	photo.setPixel(x, (int) (photo.getHeight()*.65), red);
		            }

		            // Draw vertical red lines around area scanned
		            for(int y = (int)(photo.getHeight()*.35); y<(int)(photo.getHeight()*.65); y++) {
		            	photo.setPixel((int) (photo.getHeight()*.35), y, red);
		            	photo.setPixel((int) (photo.getHeight()*.65), y, red);
		            }

		            mImageView.setImageBitmap(photo);
		        }

		        File f = new File(mImageCaptureUri.getPath());

		        if (f.exists()) f.delete();

		        display();
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

	}
    public void display() {
    	Resources res = getResources();
    	Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();

    	int xmin = (int) (bitmap.getWidth() * 0.35),
    		xmax = (int) (bitmap.getWidth() * 0.65),
    	    ymin = (int) (bitmap.getHeight() * 0.35),
    	    ymax = (int) (bitmap.getHeight() * 0.65);
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

    	// Correct for possible overflow from rounding errors
    	if(redAvg > 255) redAvg = 255;
    	if(blueAvg > 255) blueAvg = 255;
    	if(greenAvg > 255) greenAvg = 255;

    	Drawable drawable = res.getDrawable(R.drawable.test);
    	drawable.setColorFilter(Color.rgb((int)redAvg,(int)greenAvg,(int)blueAvg), PorterDuff.Mode.MULTIPLY);
        display = (ImageView) findViewById(R.id.test);
    	display.setImageDrawable(drawable);
    	display.layout(200,200,200,200);

    	int pixelAvg = (int)(redAvg);
    	pixelAvg = (int)((pixelAvg << 8) + greenAvg);
    	pixelAvg = (int)((pixelAvg << 8) + blueAvg);

        TextView textView = (TextView) findViewById(R.id.text_result);

        double metric = computeLeukocoriaMetric(pixelAvg);
        if (metric < 1) {
            textView.setText("Based on the average colors of the cropped image, the chance of Leukocoria is very low.");
        } else if (metric < 3) {
            textView.setText("Based on the average colors of the cropped image, our Leukocoria test is inconclusive.");
        } else{
            textView.setText("Based on the average colors of the cropped image, the chance of Leukocoria is very high.");
        }
    }

    public double computeLeukocoriaMetric(int pixel){
    	float hsv[] = new float[3];
        Color.colorToHSV(pixel, hsv);
        double h = hsv[0];
        double s = hsv[1];
        double v = hsv[2];
        if (h > 0.8) h -= 1;

        double metric_h = 1 - (h / 360.0 - 0.2) * (h / 360.0 - 0.2);
        if (metric_h < 0.1) metric_h = 0.1;
        double metric = metric_h / (2 * s * s + (1 - v) * (1 - v) + 0.01);
        return metric;
    }
}