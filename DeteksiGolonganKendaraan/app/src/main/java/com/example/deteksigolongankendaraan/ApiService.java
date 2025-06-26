package com.example.deteksigolongankendaraan;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("/upload") // Sesuaikan dengan endpoint di server Anda
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("description") RequestBody description
    );
}