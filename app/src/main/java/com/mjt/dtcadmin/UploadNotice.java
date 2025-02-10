package com.mjt.dtcadmin;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadNotice extends AppCompatActivity {

    private MaterialCardView addimage, noticeImageview;
    private ImageView previewImageView;
    private TextInputEditText noticetittle;
    private MaterialButton uploadnoticebtn;
    private static final int REQ = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Bitmap bitmap;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private String downloadUrl = "";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_notice);

        reference = FirebaseDatabase.getInstance().getReference().child("Notice");
        storageReference = FirebaseStorage.getInstance().getReference();

        pd = new ProgressDialog(this);

        addimage = findViewById(R.id.addimage);
        noticeImageview = findViewById(R.id.noticeImageview);
        noticetittle = findViewById(R.id.noticetittle);
        uploadnoticebtn = findViewById(R.id.uploadnoticebtn);

        previewImageView = new ImageView(this);
        previewImageView.setLayoutParams(new MaterialCardView.LayoutParams(
                MaterialCardView.LayoutParams.MATCH_PARENT,
                MaterialCardView.LayoutParams.MATCH_PARENT
        ));
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        noticeImageview.addView(previewImageView);

        addimage.setOnClickListener(v -> checkStoragePermission());
        uploadnoticebtn.setOnClickListener(v -> uploadNotice());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage, REQ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                previewImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e("UploadNotice", "Error loading image: " + e.getMessage());
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadNotice() {
        String title = noticetittle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter a notice title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bitmap == null) {
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show();
        } else {
            uploadImage();
        }
    }

    private void uploadImage() {
        pd.setMessage("Uploading...");
        pd.show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] finalImage = baos.toByteArray();

        final StorageReference filepath = storageReference.child("Notice").child(System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = filepath.putBytes(finalImage);

        uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            pd.dismiss();
                            downloadUrl = uri.toString();
                            Log.d("FirebaseUpload", "Image Uploaded: " + downloadUrl);
                            uploadData();
                        })
                        .addOnFailureListener(e -> {
                            pd.dismiss();
                            Toast.makeText(UploadNotice.this, "Failed to get URL", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(UploadNotice.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadData() {
        String title = noticetittle.getText().toString();

        if (title.isEmpty() || downloadUrl.isEmpty()) {
            Toast.makeText(this, "Error: Title or Image URL is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String uniqKey = reference.push().getKey();
        if (uniqKey == null) {
            Toast.makeText(this, "Failed to generate unique key", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String date = dateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String time = timeFormat.format(calForTime.getTime());

        NoticeData noticeData = new NoticeData(title, downloadUrl, date, time, uniqKey);

        reference.child(uniqKey).setValue(noticeData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UploadNotice.this, "Notice Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    Log.d("FirebaseUpload", "Upload Successful: " + uniqKey);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadNotice.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseUpload", "Error: " + e.getMessage());
                });
    }
}
