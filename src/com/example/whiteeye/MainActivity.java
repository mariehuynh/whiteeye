package com.example.whiteeye;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import 
com.paypal.android.sdk.payments.PayPalPayment;
import 
com.paypal.android.sdk.payments.PayPalService;
import 
com.paypal.android.sdk.payments.PaymentActivity;

public class MainActivity extends Activity {
	private Uri mImageCaptureUri;
	private ImageView mImageView;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private static final int RESULT_PAYMENT_INVALID = 0;
	private static final int RESULT_OK = 5;
	private static final int RESULT_CANCELLED = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        final String [] items			= new String [] {"Take from camera", "Select from gallery"};
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
//PayPal
	    Intent intent = new Intent(this, PayPalService.class);

	    // live: don't put any environment extra
	    // sandbox: use PaymentActivity.ENVIRONMENT_SANDBOX
	    intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_SANDBOX);

	    intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "ATGFoBAoBX_fdgrxqShtz7G9DzfZ8JoqbJ9GqUFfpvLLxqeqQ_VwRyvizOhd");

	    startService(intent);
	    
	    
//End PayPal
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

		        break;
		    case RESULT_OK:   //PayPal
		    	
		        
		       
		                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
		                if (confirm != null) {
		                    try {
		                        Log.i("paymentExample", confirm.toJSONObject().toString(4));

		                        // TODO: send 'confirm' to your server for verification.
		                        // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
		                        // for more details.

		                    } catch (JSONException e) {
		                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
		                    }
		                }
		            
	    			break;
	    case RESULT_CANCELLED: //PayPal
		            
		                Log.i("paymentExample", "The user canceled.");
		                
		                break;
	    case RESULT_PAYMENT_INVALID: //PayPal
		           
		                Log.i("paymentExample", "An invalid payment was submitted. Please see the docs.");
		                break;
		        

	    }
	}
    
    //PayPal
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
    //End PayPal
    
    //PayPal
    public void onDonatePressed(View pressed) {
    	
    	//PayPalPayment payment = new PayPalPayment();
    	PayPalPayment payment = new PayPalPayment(new BigDecimal("20"), "USD", "Retinoblastoma.net suggested donation");
    	//Double your contribution if your employer has a Matching Gift Program!
        
       
        //PayPalPayment payment = new PayPalPayment(new BigDecimal("8.75"), "USD", "research help");

        Intent intent = new Intent(this, PaymentActivity.class);

        // comment this line out for live or set to PaymentActivity.ENVIRONMENT_SANDBOX for sandbox
        intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_SANDBOX);

        // it's important to repeat the clientId here so that the SDK has it if Android restarts your
        // app midway through the payment UI flow.
        intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "ATGFoBAoBX_fdgrxqShtz7G9DzfZ8JoqbJ9GqUFfpvLLxqeqQ_VwRyvizOhd");

        // Provide a payerId that uniquely identifies a user within the scope of your system,
        // such as an email address or user ID.
        intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, "<someuser@somedomain.com>");

        intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, "nguyentiffanyus-facilitator@yahoo.com");
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, 0);
    }
    
    
//End PayPal

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
}