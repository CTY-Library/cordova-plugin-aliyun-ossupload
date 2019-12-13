var exec = require('cordova/exec');

var aliyunOSSuploads = {
  //上传  
  onOssNormalPut : function(
    success,
    error,
    dataToken,
    bucket,
    object,
    localFile
  ) {
    exec(success, error, 'FileUpload', 'onOssNormalPut', [dataToken,bucket,object,localFile]);
  },
   //下载
   onOssNormalGet : function(
    success,
    error,
    dataToken,
    bucket,
    object,
    objectDownLoadKey
  ) {
    exec(success, error, 'FileUpload', 'onOssNormalGet', [dataToken,bucket,object,objectDownLoadKey]);
  },
  //取消上传/下载任务
  // onOssNormalCancel : function(
  //   success,
  //   error,
  // ) {
  //   exec(success, error, 'FileUpload', 'onOssNormalCancel', []);
  // },
  //图片缩放
  onOssImgResize : function(
    success,
    error,
    dataToken,
    bucket,
    object,
    width,
    height,
    objectDownLoadKey
  ) {
    exec(success, error, 'FileUpload', 'onOssImgResize', [dataToken,bucket,object,width,height,objectDownLoadKey]);
  },
  //图片水印
  onOssWatermark : function(
    success,
    error,
    dataToken,  
    bucket,
    object,
    waterMark,
    size,
    objectDownLoadKey
  ) {
    exec(success, error, 'FileUpload', 'onOssWatermark', [dataToken,bucket,object,waterMark,size,objectDownLoadKey]);
  }
}

module.exports = aliyunOSSuploads

 
