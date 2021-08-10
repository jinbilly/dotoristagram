package org.techtown.dotoristagram.retrofit;


import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Result;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface RetrofitAPI {
 /*
    @GET("/dotoristagram/retrofitExample.php")
    Call<List<userInfo>> getData(@Query("userId") String id);


    @FormUrlEncoded
    @POST("/dotoristagram/registerRetrofit.php")
    Call<userInfo> registerData(@FieldMap HashMap<String, Object> param);
*/

    //id 중복 확인을 위하여 보내는 id
    @GET("/dotoristagram/IdCheck.php")
    Call<String> IdCheck(@Query("userId") String id);

    //핸드폰 인증을 위해 보내는 핸드폰번호
    @GET("/dotoristagram/ncloudSENSAPI.php")
    Call<String> sendSms(@Query("phoneNum") String phoneNum);

    //로그인 절차를 위해 보내는 아이디와 비밀번호
    @FormUrlEncoded
    @POST("/dotoristagram/loginRetrofit.php")
    Call<String> loginAction(@Field("userId") String id, @Field("userPw") String pw);

    //프사 이미지만을 변경하기 위해 보내는 이미지 uri데이터
    /*
    @Multipart
    @FormUrlEncoded
    @POST("/dotoristagram/changeOnlyProfileImage.php")
    Call<String> changeOnlyProfileImage(@Part MultipartBody.Part uploaded_file, @Field("userId") String id);
*/

    //프로필 사진 변경(사진 1개)
    @Multipart
    @POST("/dotoristagram/changeOnlyProfileImage.php")
    Call<String> changeOnlyProfileImage(@Part MultipartBody.Part uploaded_file, @Part("userId") String id);


    //회원가입 부분에서 유저 데이터를 전송하기 위함
    @POST("/dotoristagram/registerRetrofit.php")
    Call<String> registerData(@Body UserInfo userInfo);

    //유저 마이페이지 정보를 조회하기 위한 데이터 날리기 위함
    @GET("/dotoristagram/myPage.php")
    Call<String> inquiryUserData(@Query("userId") String id);


    //게시물 업로드(사진 여러개, 글자)
    @Multipart
    @POST("/dotoristagram/posting.php")
    Call<String> posting(@Part List<MultipartBody.Part> uploaded_file, @Part("userId") String id, @Part("postContents") String contents);

}
