package com.example.deteksigolongankendaraan;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DualUpload {

    private ResultCallback callback;
    private Context context;


    public DualUpload(Context context, ResultCallback callback) {
        this.context = context;
        this.callback = callback;
    }



    public interface ResultCallback {
        void onResult(String resultString);
        void onError(String errorMessage);
    }

    private String getHargaByGolongan(String golongan) {
        switch (golongan) {
            case "Golongan II":
                return "Rp.  62.100";
            case "Golongan IVA":
                return "Rp.  481.800";
            case "Golongan IVB":
                return "Rp.  447.800";
            case "Golongan VA":
                return "Rp.  Rp 963.800";
            case "Golongan VB":
                return "Rp.  835.300";
            case "Golongan VIA":
                return "Rp.  1.594.800";
            case "Golongan VIB":
                return "Rp.  1.285.200";
            case "Golongan VII":
                return "Rp.  1.860.400";
            case "Golongan VIII":
                return "Rp.  2.452.400";
            default:
                return "Rp. -";
        }
    }

    private int getIconByGolongan(String golongan) {
        switch (golongan) {
            case "Golongan II":
                return R.drawable.ic_gol_2_motorcycle;
            case "Golongan IVA":
                return R.drawable.ic_gol_4a_passenger_car;
            case "Golongan IVB":
                return R.drawable.ic_gol_4b_pickup;
            case "Golongan VA":
                return R.drawable.ic_gol_5a_medium_bus;
            case "Golongan VB":
                return R.drawable.ic_gol_5b_medium_truck;
            case "Golongan VIA":
                return R.drawable.ic_gol_6a_large_bus;
            case "Golongan VIB":
                return R.drawable.ic_gol_6a_large_bus;
            case "Golongan VII":
                return R.drawable.ic_gol_7_tronton;
            case "Golongan VIII":
                return R.drawable.ic_gol_8_tronton_large;
            default:
                return R.drawable.ic_suv;
        }
    }


    public void uploadImageToRoboflow(File imageFile) {
        new Thread(() -> {
            try {
                // Base64 Encode
                String encodedFile;
                FileInputStream fileInputStreamReader = new FileInputStream(imageFile);
                byte[] bytes = new byte[(int) imageFile.length()];
                fileInputStreamReader.read(bytes);
                encodedFile = Base64.encodeToString(bytes, Base64.DEFAULT);

                String API_KEY = "thH8Z9MeexkQQTjfwJjz";
                String DATASET_NAME = "/vehicle-classification-zcpix/4";

                String uploadURL = "https://serverless.roboflow.com" +
                        DATASET_NAME +
                        "?api_key=" + API_KEY +
                        "&name=" + imageFile.getName() +
                        "&split=train";

                HttpURLConnection connection = null;

                try {
                    URL url = new URL(uploadURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length",
                            Integer.toString(encodedFile.getBytes(StandardCharsets.US_ASCII).length));
                    connection.setRequestProperty("Content-Language", "en-US");
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(encodedFile);
                    wr.close();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                    JSONArray predictions = jsonResponse.getJSONArray("predictions");

                    String detectedClass = null;
                    if (predictions.length() > 0) {
                        JSONObject firstPrediction = predictions.getJSONObject(0);
                        detectedClass = firstPrediction.getString("class");
                        double confidence = firstPrediction.getDouble("confidence");

                        String result = detectedClass + " (Confidence: " + String.format("%.2f", confidence) + ")";
                        if (callback != null) {
                            callback.onResult(result);
                            Log.d("UPLOAD", "Selesai parsing. Detected: " + detectedClass);
                            Intent intent = new Intent(context, ResultActivity.class);
                            intent.putExtra("GOLONGAN", detectedClass);
                            intent.putExtra("HARGA", getHargaByGolongan(detectedClass));
                            intent.putExtra("ICON_RES_ID", getIconByGolongan(detectedClass));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }

                    } else {
                        if (callback != null) {
                            callback.onError("No predictions found");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        Log.e("UPLOAD", "Gagal buka ResultActivity: " + e.getMessage(), e);
                        callback.onError("Upload failed: " + e.getMessage());
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError("Upload failed: " + e.getMessage());
                }
            }
        }).start();
    }

}