package com.plugin.aliyun;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.encoders.Base64;

import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * This class echoes a string called from JavaScript.
 */
public class FileUpload extends CordovaPlugin {
  private String mkey = "";
  private String mAuthData = "";
  private String mEndpoint = "";
  //字体，默认文泉驿正黑，可以根据文档自行更改
  private static final String font = "d3F5LXplbmhlaQ==";
  private static Context context_all;
  private String appName;
  /**
   * Called after plugin construction and fields have been initialized. Prefer to
   * use pluginInitialize instead since there is no value in having parameters on
   * the initialize() function.
   *
   * @param cordova
   * @param webView
   */
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Context context = this.cordova.getActivity().getApplicationContext();
    context_all = context;
    ApplicationInfo applicationInfo = null;
    try {
      applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    mkey = applicationInfo.metaData.getString("com.plugin.aliyun.SECRET_KEY");
  }

  @Override
  public boolean execute(String action, JSONArray args,final CallbackContext callbackContext) throws JSONException {
    //普通上传
    if (action.equals("onOssNormalPut")) {
      String data = args.getString(0);
      String bucket = args.getString(1);
      String object = args.getString(2);
      String localFile = args.getString(3);
      this.asyncPutObject(data, bucket, object, localFile, null, callbackContext);
      return true;
    }
    //普通下载
    else if (action.equals("onOssNormalGet")) {
        String data = args.getString(0);
        String bucket = args.getString(1);
        String object = args.getString(2);
        String objectDownLoadKey = args.getString(3); // 例如:  123.mp3
        this.onOssNormalGet(data, bucket, object, objectDownLoadKey, null, callbackContext);
        return true;
    }
    //取消上传/下载任务 onOssNormalCancel
    else if (action.equals("onOssNormalCancel")) {
      String data = args.getString(0);
      String bucket = args.getString(1);
      String object = args.getString(2);
      String objectDownLoadKey = args.getString(3);
      this.onOssNormalGet(data, bucket, object, objectDownLoadKey, null, callbackContext);
      return true;
    }
    //图片缩放
    else if (action.equals("onOssImgResize")) {
      String data = args.getString(0);
      String bucket = args.getString(1);
      String object = args.getString(2);
      String width = args.getString(3);
      String height = args.getString(4);
      String objectDownLoadKey = args.getString(5);
      this.onOssImgResize(data, bucket, object, width, height, objectDownLoadKey, null, callbackContext);
      return true;
    }
    //图片水印
    else if (action.equals("onOssWatermark")) {
      String data = args.getString(0);
      String bucket = args.getString(1);
      String object = args.getString(2);
      String waterMark = args.getString(3);
      String size = args.getString(4);
      String objectDownLoadKey = args.getString(5);
      this.onOssWatermark(data, bucket, object,waterMark,size, objectDownLoadKey, null, callbackContext);
      return true;
    }
    return false;
  }

  //图片缩放
  private void onOssImgResize(String data, String mBucket, String object, String width, String height, final String objectDownLoadKey,
                             final String callbackAddress,final CallbackContext callbackContext) {
    String queryString = "@" + String.valueOf(width) + "w_" + String.valueOf(height) + "h_1e_1c";
    onOssNormalGet(data, mBucket, object + queryString, objectDownLoadKey, null, callbackContext);
  }


  //图片水印
  private void onOssWatermark(String data, String mBucket, String object, String waterMark, String size, final String objectDownLoadKey,
                              final String callbackAddress,final CallbackContext callbackContext) {
    String base64Text = android.util.Base64.encodeToString(waterMark.getBytes(), android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);

    String queryString = "@400w|watermark=2&type=" + font + "&text=" + base64Text + "&size=" + String.valueOf(size);
    onOssNormalGet(data, mBucket, object + queryString, objectDownLoadKey, null, callbackContext);
  }


  private String aesDecrypt(String str, String key) throws Exception {
    if (str == null || key == null)
      return null;
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
    byte[] bytes = Base64.decode(str.getBytes("utf-8"));
    bytes = cipher.doFinal(bytes);
    return new String(bytes, "utf-8");
  }

  //获取文件
  private void onOssNormalGet(String data, String mBucket, String object,final String objectDownLoadKey,
                              final String callbackAddress,final CallbackContext callbackContext) {
    if (data.equals("")) {
      callbackContext.error("Expected one non-empty string argument data.");
      return;
    }

     if (objectDownLoadKey.equals("")) {
         callbackContext.error("Expected one non-empty string argument objectDownLoadKey.");
         return;
     }

    if (mBucket.equals("")) {
      callbackContext.error("Expected one non-empty string argument bucket.");
      return;
    }

    if (object.equals("")) {
      callbackContext.error("Expected one non-empty string argument object.");
      return;
    }

    try {
      mAuthData = this.aesDecrypt(data, mkey);
      JSONObject jsonObj_point = new JSONObject(mAuthData);
      mEndpoint =  jsonObj_point.getString("Endpoint");
    } catch (Exception e) {
      callbackContext.error(e.toString());
      return;
    }

    OSSLog.logDebug("create GetObjectRequest");
    GetObjectRequest get = new GetObjectRequest(mBucket, object);
    get.setCRC64(OSSRequest.CRC64Config.YES);
    get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
      @Override
      public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
        Log.d("GetObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
//        int progress = (int) (100 * currentSize / totalSize);
//        mDisplayer.updateProgress(progress);
//        mDisplayer.displayInfo("下载进度: " + String.valueOf(progress) + "%");
      }
    });
    OSSLog.logDebug("asyncGetObject");
    // 构造上传请求
    OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("") {
      @Override
      public OSSFederationToken getFederationToken() throws ClientException {
        OSSFederationToken authToken;
        try {

          JSONObject jsonObj = new JSONObject(mAuthData);
          int statusCode = jsonObj.getInt("StatusCode");
          if (statusCode == 200) {
            String ak = jsonObj.getString("AccessKeyId");
            String sk = jsonObj.getString("AccessKeySecret");
            String token = jsonObj.getString("SecurityToken");
            String expiration = jsonObj.getString("Expiration");
            mEndpoint =  jsonObj.getString("Endpoint");
            authToken = new OSSFederationToken(ak, sk, token, expiration);
          } else {
            String errorCode = jsonObj.getString("ErrorCode");
            String errorMessage = jsonObj.getString("ErrorMessage");
            throw new ClientException("ErrorCode: " + errorCode + "| ErrorMessage: " + errorMessage);
          }
          return authToken;
        } catch (Exception e) {
          throw new ClientException(e);
        }
      }
    };
    ClientConfiguration conf = new ClientConfiguration();
    conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
    conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
    conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
    conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
    OSS oss = new OSSClient(this.cordova.getActivity().getApplicationContext(), mEndpoint, credentialProvider, conf);
    OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
      @Override
      public void onSuccess(GetObjectRequest request, GetObjectResult result) {
        // 请求成功
        InputStream inputStream = result.getObjectContent();
        //Bitmap bm = BitmapFactory.decodeStream(inputStream);
        try {
          SaveFile(inputStream,objectDownLoadKey);
          callbackContext.success("success"); //成功回调
          //需要根据对应的View大小来自适应缩放
         // Bitmap bm = mDisplayer.autoResizeFromStream(inputStream);
          //long get_end = System.currentTimeMillis();
         // OSSLog.logDebug("get cost: " + (get_end - get_start) / 1000f);
//          mDisplayer.downloadComplete(bm);
//          mDisplayer.displayInfo("Bucket: " + mBucket + "\nObject: " + request.getObjectKey() + "\nRequestId: " + result.getRequestId());
        } catch (IOException e) {
          e.printStackTrace();
          callbackContext.error("文件存储异常:"+e.getMessage()); //失败
        }
      }

      @Override
      public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
        String info = "";
        // 请求异常
        if (clientExcepion != null) {
          // 本地异常如网络异常等
          clientExcepion.printStackTrace();
          info = clientExcepion.toString();
          callbackContext.error("本地异常如网络异常等:"+info); //失败
        }
        if (serviceException != null) {
          // 服务异常
          Log.e("ErrorCode", serviceException.getErrorCode());
          Log.e("RequestId", serviceException.getRequestId());
          Log.e("HostId", serviceException.getHostId());
          Log.e("RawMessage", serviceException.getRawMessage());
          info = serviceException.toString();
          callbackContext.error("服务异常::"+info); //失败
        }
//        mDisplayer.downloadFail(info);
//        mDisplayer.displayInfo(info);
      }
    });
  }
  public static void SaveFile(InputStream is,String fileName) throws IOException{
    Resources appResource = context_all.getResources();
    String appName = appResource.getText(appResource.getIdentifier("app_name", "string", context_all.getPackageName())).toString();
    String state = Environment.getExternalStorageState();
    File directory = null;
    if(Environment.MEDIA_MOUNTED.equals(state)){
      directory = context_all.getExternalFilesDir(appName);
    }else{
      directory = context_all.getFilesDir();
    }
    if(!directory.exists()){
      directory.mkdir();
    }
    File mp3File = new File(directory, String.valueOf(fileName));
    BufferedInputStream in=null;
    BufferedOutputStream out=null;
    in=new BufferedInputStream(is);
    out=new BufferedOutputStream(new FileOutputStream(mp3File));
    int len=-1;
    byte[] b=new byte[1024];
    while((len=in.read(b))!=-1){
      out.write(b,0,len);
    }
    in.close();
    out.close();
  }

  private void asyncPutObject(String data, String bucket, String object, String localFile,
                              final String callbackAddress,final CallbackContext callbackContext) {
    if (data.equals("")) {
      callbackContext.error("Expected one non-empty string argument data.");
      return;
    }

    // if (endPoint.equals("")) {
    //     callbackContext.error("Expected one non-empty string argument endPoint.");
    //     return;
    // }

    if (bucket.equals("")) {
      callbackContext.error("Expected one non-empty string argument bucket.");
      return;
    }

    if (object.equals("")) {
      callbackContext.error("Expected one non-empty string argument object.");
      return;
    }

    try {
      mAuthData = this.aesDecrypt(data, mkey);
      JSONObject jsonObj_point = new JSONObject(mAuthData);
      mEndpoint =  jsonObj_point.getString("Endpoint");
    } catch (Exception e) {
      callbackContext.error(e.toString());
      return;
    }

    File file = new File(localFile);
    if (!file.exists()) {
      callbackContext.error("Expected one non-empty string argument localFile.");
      return;
    }

    // 构造上传请求
    OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider("") {
      @Override
      public OSSFederationToken getFederationToken() throws ClientException {
        OSSFederationToken authToken;
        try {

          JSONObject jsonObj = new JSONObject(mAuthData);
          int statusCode = jsonObj.getInt("StatusCode");
          if (statusCode == 200) {
            String ak = jsonObj.getString("AccessKeyId");
            String sk = jsonObj.getString("AccessKeySecret");
            String token = jsonObj.getString("SecurityToken");
            String expiration = jsonObj.getString("Expiration");
            mEndpoint =  jsonObj.getString("Endpoint");
            authToken = new OSSFederationToken(ak, sk, token, expiration);
          } else {
            String errorCode = jsonObj.getString("ErrorCode");
            String errorMessage = jsonObj.getString("ErrorMessage");
            throw new ClientException("ErrorCode: " + errorCode + "| ErrorMessage: " + errorMessage);
          }
          return authToken;
        } catch (Exception e) {
          throw new ClientException(e);
        }
      }
    };
    ClientConfiguration conf = new ClientConfiguration();
    conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
    conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
    conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
    conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
    OSS oss = new OSSClient(this.cordova.getActivity().getApplicationContext(), mEndpoint, credentialProvider, conf);
    PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);
    put.setCRC64(OSSRequest.CRC64Config.YES);
    if (callbackAddress != null) {
      // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
      put.setCallbackParam(new HashMap<String, String>() {
        {
          put("callbackUrl", callbackAddress);
          // callbackBody可以自定义传入的信息
          put("callbackBody", "filename=${object}");
        }
      });
    }
    // 异步上传时可以设置进度回调
    put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
      @Override
      public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
        int progress = (int) (100 * currentSize / totalSize);
      }
    });
    OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
      @Override
      public void onSuccess(PutObjectRequest request, PutObjectResult result) {
        callbackContext.success("success");
      }

      @Override
      public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                            ServiceException serviceException) {
        String info = "";
        // 请求异常
        if (clientExcepion != null) {
          // 本地异常如网络异常等
          clientExcepion.printStackTrace();
          info = clientExcepion.toString();
        }
        if (serviceException != null) {
          // 服务异常
          info = serviceException.toString();
        }
        callbackContext.error(info);
      }
    });
  }
}
