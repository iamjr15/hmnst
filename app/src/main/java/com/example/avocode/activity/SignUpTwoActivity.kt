package com.example.avocode.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.Glide
import com.example.avocode.R
import com.example.avocode.utils.Util
import com.example.avocode.utils.Util.Companion.checkEmptyStrings
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_login.buttonNext
import kotlinx.android.synthetic.main.activity_sign_up2.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//Second signup activity to take user's profile picture
class SignUpTwoActivity : AppCompatActivity() {
    private var uriPath: String? = null
    private var util: Util? = null


    private// Create the storage directory if it does not exist
    // Create a media file name
    val outputMediaFile: File?
        get() {
            val mediaStorageDir = File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + applicationContext.packageName + "/Files")
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            val timeStamp = SimpleDateFormat("ddMMyyyy_HHmm", Locale.ENGLISH).format(Date())
            val mediaFile: File
            val mImageName = "MI_$timeStamp.jpg"
            mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
            return mediaFile
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)
        util = Util(this)
        buttonNext.setOnClickListener {
            if (!checkEmptyStrings(uriPath)) {
                val intent = Intent(this@SignUpTwoActivity, SignUpThreeActivity::class.java)
                intent.putExtra(getString(R.string.firstName), getIntent().getStringExtra(getString(R.string.firstName)))
                intent.putExtra(getString(R.string.last_name), getIntent().getStringExtra(getString(R.string.last_name)))
                intent.putExtra(getString(R.string.gender_label), getIntent().getStringExtra(getString(R.string.gender_label)))
                intent.putExtra(getString(R.string.dob), getIntent().getStringExtra(getString(R.string.dob)))
                intent.putExtra(getString(R.string.password_label), getIntent().getStringExtra(getString(R.string.password_label)))
                intent.putExtra(getString(R.string.phone_label), getIntent().getStringExtra(getString(R.string.phone_label)))
                intent.putExtra(getString(R.string.uriPath), uriPath)
                startActivity(intent)
            } else {
                util!!.toast(getString(R.string.message_add_picture))
            }
        }
        imageView.setOnClickListener {
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity().setRequestedSize(500, 500, CropImageView.RequestSizeOptions.RESIZE_INSIDE).start(this)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                if (resultUri != null) {
                    uriPath = resultUri.path
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (bitmap != null) {
                        storeImage(bitmap)
                        Glide.with(this).load(resultUri).into(imageView)
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                util!!.toast(result.error.localizedMessage)
            }
        }
    }

    private fun storeImage(image: Bitmap) {
        val pictureFile = outputMediaFile
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ")
            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            uriPath = pictureFile.absolutePath
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: " + e.message)
        }

    }

    companion object {

        private val TAG = SignUpThreeActivity::class.java.simpleName
    }
}
