package com.example.escortme.network;


import com.example.escortme.network.model.AlertRequest;
import com.example.escortme.network.model.AuthRequest;
import com.example.escortme.network.model.Comment;
import com.example.escortme.network.model.DriverRequest;
import com.example.escortme.network.model.EmergencyRequest;
import com.example.escortme.network.model.ReviewRequest;
import com.example.escortme.network.model.StudentInfo;
import com.example.escortme.network.model.TripRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface API {
    // Registration functionality
    @POST("auth/register-student")
    Call<ResponseBody> registerUser(@Body AuthRequest request);

    // Login functionality
    @POST("auth/login")
    Call<ResponseBody> signInUser(@Body AuthRequest request);

    // Create a new media alert in the community

    @Multipart
    @POST("student/{studentId}/new-media-alert")
    //@Headers("Content-Type:application/form-data; charset=utf-8")
    Call<ResponseBody> submitMediaTip(@Path("studentId") Long id,
                                      @Part("tag") String tag,
                                      @Part("body") String body,
                                      @Part List<MultipartBody.Part> files
                                      );

    //  @Part("alert") AlertRequest request
    // Create a new alert in the community
    @POST("student/{studentId}/new-alert")
    Call<ResponseBody> submitTip(@Path("studentId") Long id,
                                 @Body AlertRequest request
    );


    // Retrieve a list of all the unblocked alerts
    @GET("student/{orgId}/alerts")
    Call<ResponseBody> getAlerts(@Path("orgId") Long orgId);

    @GET("student/{orgId}/alerts/{tag}")
    Call<ResponseBody> getAlertsByTag(@Path("orgId") Long orgId, @Path("tag") String tag);

    // Retrieve all the comments that are associated with one alert
    @GET("student/alerts/{alertId}/comments")
    Call<ResponseBody> getAllCommentsByAlertId(@Path("alertId") Long alertId);

    // Add a new comment
    @POST("student/alerts/{alertId}/{studentId}/comments")
    Call<ResponseBody> addComment(@Path("alertId") Long alertId, @Path("studentId") Long studentId, @Body Comment comment);

    // Send trip request
    @POST("student/{orgId}/{userId}/request-trip")
    Call<ResponseBody> requestEscort(@Path("orgId") Long orgId, @Path("userId") Long userId, @Body TripRequest tripRequest);


    // Get current trip info



    @PUT("student/{studentId}/{userId}/update-student")
    Call<ResponseBody> updateStudentInfo(@Path("studentId") Long studentId,
                                         @Path("userId") Long userId,
                                         @Body StudentInfo studentInfo);

    @GET("student/{userId}/student-id")
    Call<ResponseBody> getStudentId(@Path("userId") Long userId);


    @GET("student/{userId}/student-details")
    Call<ResponseBody> getStudentDetails(@Path("userId") Long userId);


    @GET("student/{userId}/student-trips")
    Call<ResponseBody> getAllStudentTrips(@Path("userId") Long userId);


    /*
     * Driver app functionality
     */

    @Multipart
    @POST("driver/{driverId}/upload-profile-image")
    Call<ResponseBody> uploadDriverImage(@Path("driverId") Long id,
                                         @Part MultipartBody.Part file);

    // Retrieves all the trips assigned to a driver
    @GET("driver/{driverId}/available-requests")
    Call<ResponseBody> getAllRequests(@Path("driverId") Long driverId);

    @GET("driver/{driverId}/trips")
    Call<ResponseBody> getAllDriverTrips(@Path("driverId") Long driverId);

    // Completing a trip
    @PUT("driver/{tripId}/accept-trip")
    Call<ResponseBody> acceptTrip(@Path("tripId") Long tripId);

    // Cancelling a trip
    @PUT("driver/{tripId}/cancel-trip")
    Call<ResponseBody> cancelTrip(@Path("tripId") Long tripId);


    @GET("driver/{driverId}/driver-info")
    Call<ResponseBody> getDriverInfo(@Path("driverId") Long driverId);

    @PUT("admin/{driverId}/update-driver")
    Call<ResponseBody> updateDriver(@Path("driverId") Long driverId, @Body DriverRequest driverRequest);

    @GET("student/{orgId}/org-driver-size")
    Call<ResponseBody> getOrgSize(@Path("orgId") Long orgId);

    @POST("student/{studentId}/{driverId}/new-review")
    Call<ResponseBody> createNewReview(@Path("studentId") Long studentId,
                                       @Path("driverId") Long driverId,
                                       @Body ReviewRequest reviewRequest);


    @GET("admin/{driverId}/view-driver-details")
    Call<ResponseBody> allDriverTips(@Path("driverId") Long driverId);

    @GET("driver/{orgId}/org-emergencies")
    Call<ResponseBody> getAllOrgEmergencies(@Path("orgId") Long orgId);


    @GET("student/{studentId}/all-emergencies")
    Call<ResponseBody> getAllStudentEmergencies(@Path("studentId") Long studentId);

    @POST("student/{studentId}/{orgId}/new-emergency")
    Call<ResponseBody> createNewEmergency(@Path("studentId") Long studentId,
                                          @Path("orgId") Long orgId,
                                          @Body EmergencyRequest emergencyRequest);


    @GET("driver/{orgId}/notifications")
    Call<ResponseBody> getOrgNotifications(@Path("orgId") Long orgId);

    @GET("student/{orgId}/emergency-points")
    Call<ResponseBody> getAllEmergencyPoints(@Path("orgId") Long orgId);


}
