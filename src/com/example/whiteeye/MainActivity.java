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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;


import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.PayPalPayment;


public class MainActivity extends Activity {
    private Uri mImageCaptureUri;
    private ImageView mImageView;
    private ImageView display;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int DONATE_PAYPAL = 4;

    private static final int RESULT_PAYMENT_INVALID = 1749;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        final String [] items = new String [] {"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //PayPal
        Intent intent = new Intent(this, PayPalService.class);
        // live: don't put any environment extra
        // sandbox: use PaymentActivity.ENVIRONMENT_SANDBOX
        intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, PaymentActivity.ENVIRONMENT_SANDBOX);
        intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, "ATGFoBAoBX_fdgrxqShtz7G9DzfZ8JoqbJ9GqUFfpvLLxqeqQ_VwRyvizOhd");
        startService(intent);
        //End PayPal

        builder.setTitle("Select Image");
        
        String msg = "Leukocoria is an abnormal white reflection from the retina of the eye that is indicative of retinoblastoma and other eye diseases.  This app will scan an image of an eye and compare it to data collected from patients in a recent research study.  For best results, dilate the pupils and try to crop the photo in so the red box is in the pupil.  Thanks for checking this app out!";
        
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Hello there!");
        alertDialog.setMessage(msg);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
              // TODO Add your code for the button here.
        	  // Do nothing for now
           }
        });
        // Set the Icon for the Dialog
        alertDialog.show();

        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        switch(resultCode) {
            case Activity.RESULT_CANCELED: //PayPal
                Log.i("paymentExample", "The user canceled.");

                    break;
            case RESULT_PAYMENT_INVALID: //PayPal
                Log.i("paymentExample", "An invalid payment was submitted. Please see the docs.");
                break;
        }
        if (resultCode != Activity.RESULT_OK) return;

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

            case DONATE_PAYPAL:   //PayPal
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

        startActivityForResult(intent, DONATE_PAYPAL);
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
                Intent i         = new Intent(intent);
                ResolveInfo res    = list.get(0);

                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title     = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon        = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
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
        System.out.println("Leukocoria metric: " + Double.toString(metric));
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
        if (h > 0.7) h -= 1;

        double metric_h = 1 - (h / 360.0 - 0.2) * (h / 360.0 - 0.2);
        if (metric_h < 0.1) metric_h = 0.1;
        double metric = metric_h / (2 * s * s + (1 - v) * (1 - v) + 0.01);
        return metric;
    }
}