package com.plugin.aliyun;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
 

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
            //String endPoint = args.getString(1);
            String bucket = args.getString(1);
            String object = args.getString(2);
            String localFile = args.getString(3);
            this.asyncPutObject(data, bucket, object, localFile, null, callbackContext);
            return true;
        }
        //普通下载
        else if (action.equals("onOssNormalGet")) {
            
            return false;
        }
        //取消上传/下载任务 onOssNormalCancel
        else if (action.equals("onOssNormalCancel")) {
            
            return false;
        }
        //图片缩放
        else if (action.equals("onOssImgResize")) {
            
            return false;
        }
        //图片水印
        else if (action.equals("onOssWatermark")) {
            
            return false;
        }
        return false;
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
        } catch (Exception e) {
            callbackContext.error(e.toString());
            return;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            callbackContext.error("Expected one non-empty string argument localFile.");
            return;
        }
        final Date currentTime1 = new Date();
        final SimpleDateFormat date1 = new SimpleDateFormat("初始化开始:yyyy年MM月dd日：HH:mm:ss---SSS(毫秒)");
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
