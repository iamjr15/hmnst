package com.example.avocode.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.avocode.R;
import com.example.avocode.utils.Util;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.avocode.utils.Util.checkEmptyStrings;

//Second signup activity to take user's profile picture
public class SignUpTwoActivity extends AppCompatActivity {

    private static final String TAG = SignUpThreeActivity.class.getSimpleName();
    @BindView(R.id.imageView)
    ImageView imageView;
    private String uriPath = null;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
        ButterKnife.bind(this);
        util = new Util(this);
    }

    @OnClick(R.id.nextSignUp2_btn)
    public void onNextClicked() {
        if (!checkEmptyStrings(uriPath)) {
            Intent intent = new Intent(SignUpTwoActivity.this, SignUpThreeActivity.class);
            intent.putExtra("firstName", getIntent().getStringExtra("firstName"));
            intent.putExtra("lastName", getIntent().getStringExtra("lastName"));
            intent.putExtra("gender", getIntent().getStringExtra("gender"));
            intent.putExtra("dob", getIntent().getStringExtra("dob"));
            intent.putExtra("password", getIntent().getStringExtra("password"));
            intent.putExtra("phone", getIntent().getStringExtra("phone"));
            intent.putExtra("uriPath", uriPath);
            startActivity(intent);
        } else {
            util.toast(getString(R.string.message_add_picture));
        }
    }

    @OnClick(R.id.imageView)
    public void onImageClicked() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity().setRequestedSize(500, 500, CropImageView.RequestSizeOptions.RESIZE_INSIDE).start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (resultUri != null) {
                    uriPath = resultUri.getPath();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        storeImage(bitmap);
                        Glide.with(this).load(resultUri).into(imageView);
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            uriPath = pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }


    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.ENGLISH).format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
