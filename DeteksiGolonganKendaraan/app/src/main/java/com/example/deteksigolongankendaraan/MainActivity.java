package com.example.deteksigolongankendaraan;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
// Hapus import ML Kit, karena nanti akan diganti Roboflow
// import com.google.mlkit.vision.common.InputImage;
// import com.google.mlkit.vision.objects.DetectedObject;
// ...

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    // --- UI Elements ---
    private PreviewView previewView;
    private ImageButton buttonBack;
    private ImageButton buttonFlipCamera;
    private ImageButton buttonGallery;
    private ImageButton buttonShutter;
    private FrameLayout frameProcessingOverlay;
    private LinearLayout layoutResultArea;
    private ImageView imageViewResultStatusIcon;
    private TextView textViewVehicleClassResult;

    // --- CameraX & Networking ---
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ApiService apiService;
    private final Handler resultHandler = new Handler(Looper.getMainLooper());

    private static final int REQUEST_GALLERY_IMAGE = 102;
    private Uri selectedImageUri;

    // --- Simulasi untuk Frontend ---
    private final Random random = new Random();
    // Daftar label ini akan kita gunakan untuk simulasi, sesuai dengan yang akan Anda buat di Roboflow
    private final String[] roboflowLabels = {
            "gol_1", "gol_2", "gol_3", "gol_4a", "gol_4b", "gol_5a", "gol_5b",
            "gol_6a", "gol_7", "gol_8", "gol_9", "unknown"
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isSignedUp = sharedPreferences.getBoolean("isSignedUp", false);
        Log.d("MainActivity", "isSignedUp = " + isSignedUp);



            setContentView(R.layout.activity_main);

            initUI();
            setupListeners();
            setupDependencies();


            if (allPermissionsGranted()) {
                startCamera();
            } else {
                requestRequiredPermissions();
            }


    }

    private void initUI() {
        previewView = findViewById(R.id.previewView);
        buttonBack = findViewById(R.id.button_back);
        buttonFlipCamera = findViewById(R.id.button_flip_camera);
        buttonGallery = findViewById(R.id.button_gallery);
        buttonShutter = findViewById(R.id.button_shutter);
        frameProcessingOverlay = findViewById(R.id.frame_processing_overlay);
        layoutResultArea = findViewById(R.id.layout_result_area);
        imageViewResultStatusIcon = findViewById(R.id.imageView_result_status_icon);
        textViewVehicleClassResult = findViewById(R.id.textView_vehicleClass_result);
    }



    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());
        buttonFlipCamera.setOnClickListener(v -> flipCamera());
        buttonShutter.setOnClickListener(v -> captureAndProcessImage());
        buttonGallery.setOnClickListener(v -> openGallery());
//        buttonGallery.setOnClickListener(v -> Toast.makeText(this, "Fitur Galeri akan datang!", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ hanya perlu izin READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_GALLERY_IMAGE);
            } else {
                launchGalleryIntent();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 hanya perlu izin READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY_IMAGE);
            } else {
                launchGalleryIntent();
            }
        } else {
            // Android 9- perlu izin WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_GALLERY_IMAGE);
            } else {
                launchGalleryIntent();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY_IMAGE) {
                selectedImageUri = data.getData();
                processSelectedImage();
            }
        }
    }



    private File createFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private void processSelectedImage() {
        frameProcessingOverlay.setVisibility(View.VISIBLE);

        try {
            // Ubah URI ke File agar bisa diupload
            File imageFile = createFileFromUri(selectedImageUri);
            if (imageFile == null || !imageFile.exists()) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                frameProcessingOverlay.setVisibility(View.GONE);
                return;
            }

            // Upload ke Roboflow pakai DualUpload
            new Thread(() -> {
                DualUpload uploader = new DualUpload(MainActivity.this, new DualUpload.ResultCallback() {
                    @Override
                    public void onResult(String resultString) {
                        runOnUiThread(() -> {
                            Log.d("GALLERY_UPLOAD", "Hasil Deteksi: " + resultString);

                            // Ambil golongan dari string hasil
                            String classLabel = resultString.split(" \\(")[0]; // Misal: "Gol. IVB"
                            VehicleClassInfo info = getVehicleClassInfo(classLabel);

                            showClassificationResult(info);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Gagal upload: " + errorMessage, Toast.LENGTH_SHORT).show();
                            frameProcessingOverlay.setVisibility(View.GONE);
                        });
                    }
                });

                uploader.uploadImageToRoboflow(imageFile);
            }).start();

        } catch (IOException e) {
            Log.e(TAG, "Gagal mengubah gambar: " + e.getMessage());
            Toast.makeText(this, "Gagal mengolah gambar", Toast.LENGTH_SHORT).show();
            frameProcessingOverlay.setVisibility(View.GONE);
        }
    }


    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = "vehicle_gallery_" + System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/DeteksiKendaraan");

        Uri uri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Toast.makeText(this, "Gambar disimpan", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    private void launchGalleryIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL));
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }

    private void setupDependencies() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        String BASE_URL = "http://192.168.1.5:3000"; // <<< GANTI DENGAN IP SERVER ANDA
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        apiService = retrofit.create(ApiService.class);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Gagal memulai CameraX: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void flipCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }


    private void captureAndProcessImage() {
        if (imageCapture == null) return;

        frameProcessingOverlay.setVisibility(View.VISIBLE);
        buttonShutter.setEnabled(false);

        // Buat nama file unik
        String fileName = "vehicle_" + System.currentTimeMillis() + ".jpg";

        // Simpan ke MediaStore agar bisa terlihat di Galeri
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/DeteksiKendaraan");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        Log.d(TAG, "Foto disimpan di: " + savedUri);

                        // Beritahu sistem untuk scan ulang agar muncul di galeri
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(savedUri);
                        sendBroadcast(mediaScanIntent);

                        Toast.makeText(MainActivity.this, "Foto disimpan di galeri: " + fileName, Toast.LENGTH_SHORT).show();

                        // Ambil path file dari URI
                        String filePath = getRealPathFromURI(savedUri);
                        if (filePath == null) {
                            Toast.makeText(MainActivity.this, "Gagal mendapatkan path file", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        File imageFile = new File(filePath);
                        Log.d("DEBUG", "Path file: " + filePath);
                        Log.d("DEBUG", "Path file: " + imageFile.getAbsolutePath());
                        Log.d("DEBUG", "File exists: " + imageFile.exists());
                        Log.d("DEBUG", "File length: " + imageFile.length());

                        if (!imageFile.exists() || imageFile.length() == 0) {
                            Toast.makeText(MainActivity.this, "File tidak valid atau kosong", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new Thread(() -> {
                            DualUpload uploader = new DualUpload(MainActivity.this, new DualUpload.ResultCallback() {
                                @Override
                                public void onResult(String resultString) {
                                    runOnUiThread(() -> {
                                        Log.d("DEBUG", "Masuk Thread" );
                                        Toast.makeText(MainActivity.this, "Foto berhasil dikirim: " + fileName, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(MainActivity.this,"Hasil deteksi " +  resultString, Toast.LENGTH_LONG).show();
                                    });
                                    finish();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });

                            uploader.uploadImageToRoboflow(imageFile);
                        }).start();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Gagal menyimpan gambar: " + exception.getMessage(), exception);
                        Toast.makeText(MainActivity.this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
                        frameProcessingOverlay.setVisibility(View.GONE);
                        buttonShutter.setEnabled(true);
                    }
                });

    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }



    private void showClassificationResult(VehicleClassInfo info) {
        frameProcessingOverlay.setVisibility(View.GONE); // Sembunyikan loading
        layoutResultArea.setVisibility(View.VISIBLE);
        textViewVehicleClassResult.setText(String.format("%s", info.keterangan)); // Tampilkan hanya keterangan untuk UI yang lebih bersih

        if (info.golongan.equals("N/A")) {
            imageViewResultStatusIcon.setImageResource(R.drawable.ic_error_circle);
        } else {
            imageViewResultStatusIcon.setImageResource(R.drawable.ic_checkmark_circle);
        }

        resultHandler.postDelayed(this::resetUI, 4000); // Sembunyikan hasil setelah 4 detik
    }

    private void resetUI() {
        layoutResultArea.setVisibility(View.GONE);
        buttonShutter.setEnabled(true);
    }


    private static class VehicleClassInfo {
        final String golongan;
        final String keterangan;
        VehicleClassInfo(String golongan, String keterangan) {
            this.golongan = golongan;
            this.keterangan = keterangan;
        }
    }

    private VehicleClassInfo getVehicleClassInfo(String roboflowLabel) {
        if (roboflowLabel == null) {
            return new VehicleClassInfo("N/A", "Gagal Analisis");
        }

        switch (roboflowLabel.toLowerCase(Locale.ROOT)) {
            case "gol_1":
                return new VehicleClassInfo("Gol. I", "Sepeda Kayuh Tanpa Motor");
            case "gol_2":
                return new VehicleClassInfo("Gol. II", "Sepeda Motor <500cc & Gerobak");
            case "gol_3":
                return new VehicleClassInfo("Gol. III", "Sepeda Motor >500cc & Roda 3");
            case "gol_4a":
                return new VehicleClassInfo("Gol. IVA", "Jeep, Sedan, Minibus");
            case "gol_4b":
                return new VehicleClassInfo("Gol. IVB", "Mobil Barang/Pick Up");
            case "gol_5a":
                return new VehicleClassInfo("Gol. VA", "Medium Bus / Ambulans");
            case "gol_5b":
                return new VehicleClassInfo("Gol. VB", "Truk / Tangki Sedang");
            case "gol_6a":
                return new VehicleClassInfo("Gol. VIA", "Bus Besar (AKAP, Pariwisata)");
            case "gol_7":
                return new VehicleClassInfo("Gol. VII", "Tronton / Truk Besar (10-12m)");
            case "gol_8":
                return new VehicleClassInfo("Gol. VIII", "Tronton / Alat Berat (12-16m)");
            case "gol_9":
                return new VehicleClassInfo("Gol. IX", "Tronton / Alat Berat (>16m)");
            default:
                return new VehicleClassInfo("N/A", "Tidak Teridentifikasi");
        }
    }


private boolean allPermissionsGranted() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    } else {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}


    private void requestRequiredPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION || requestCode == REQUEST_GALLERY_IMAGE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                if (requestCode == REQUEST_CAMERA_PERMISSION) {
                    startCamera();
                } else {
                    launchGalleryIntent();
                }
            } else {
                // Tampilkan penjelasan mengapa izin diperlukan
                showPermissionExplanation();
            }
        }
    }

    private void showPermissionExplanation() {
        new AlertDialog.Builder(this)
                .setTitle("Izin Diperlukan")
                .setMessage("Aplikasi membutuhkan izin untuk mengakses kamera dan penyimpanan agar dapat berfungsi dengan baik")
                .setPositiveButton("Setuju", (dialog, which) -> {
                    // Buka pengaturan aplikasi
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Tolak", null)
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        resultHandler.removeCallbacksAndMessages(null);
    }
}