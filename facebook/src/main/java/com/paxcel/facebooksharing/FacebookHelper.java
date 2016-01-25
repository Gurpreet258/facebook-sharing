package com.paxcel.facebooksharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Gurpreet on 10/21/2015.
 */
public class FacebookHelper {
    private static CallbackManager callbackManager;
    private static LoginManager loginManager;
    private static List<String> permissions;
    private static boolean isShared=false;
    private static Context context;
    private static JSONObject object;

    public static void init(Context context){
        FacebookHelper.context=context;
        FacebookHelper.callbackManager=CallbackManager.Factory.create();
        FacebookHelper.loginManager=LoginManager.getInstance();
        permissions= Arrays.asList("publish_actions");
    }
    public static void shareLink( final Uri uri,final String title){
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                ((PublishCallback)context).onPublish(publishLink(uri, title));

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
            }
        });
        loginManager.logInWithPublishPermissions((Activity)context, permissions);
    }

    public static void loginWithFacebook(){
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                ((LoginCallback)context).onLogin(object, true);

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                ((LoginCallback)context).onLogin(null, false);

            }

            @Override
            public void onError(FacebookException e) {
                ((LoginCallback)context).onLogin(null, false);
                e.printStackTrace();
            }
        });
        loginManager.logInWithReadPermissions((Activity) context, Arrays.asList("public_profile"));
    }
    public static  void shareImage(final Bitmap bitmap){
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                ((PublishCallback)context).onPublish(publishImage(bitmap));
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
            }
        });
        loginManager.logInWithPublishPermissions((Activity) context, permissions);
    }
    public static void loginWithReadPermission(Context context,List<String> permissions){
        loginManager.logInWithReadPermissions((Activity) context,permissions);
    }
    private  static boolean publishImage(Bitmap image){
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        return shareContent(content);
    }
    private static boolean publishLink(Uri uri,String title){
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(uri)
                .setContentTitle(title)
                .build();
        boolean ispublished=shareContent(content);
            return ispublished;
    }

    public static interface FriendsListCallback{
       void getFriendList(JSONObject object);
    }
    public static interface PublishCallback{
        void onPublish(boolean isPublished);
    }
    public static interface LoginCallback{
        void onLogin(JSONObject object,boolean isLogin);
    }
    public static void getFriendsList(final FriendsListCallback receiveResponse){
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/taggable_friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                object = response.getJSONObject();
                                receiveResponse.getFriendList(object);
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
            }
        });
        loginWithReadPermission(context,Arrays.asList("user_friends"));
    }

    public static boolean shareContent(ShareContent content){
        ShareApi.share(content, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                isShared = true;
            }

            @Override
            public void onCancel() {
                isShared = false;
            }

            @Override
            public void onError(FacebookException e) {
                isShared = false;
            }
        });
        return  isShared;
    }
    public static void onActivityResult(int requestCode, final int resultCode, final Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

