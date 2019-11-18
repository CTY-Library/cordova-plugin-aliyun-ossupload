var exec = require('cordova/exec');

//注意  
//参数:dataToken 是获取自己应用服务器的OssToken用AES加密后的数据
//参数:object 表示文件存储名,最好加上时间戳

//上传  
exports.onOssNormalPut = function(
  success,
  error,
  dataToken,
  endPoint,
  bucket,
  object,
  localFile
) {
  exec(success, error, 'FileUpload', 'onOssNormalPut', [
    dataToken,
    endPoint,
    bucket,
    object,
    localFile
  ]);
};


//下载
exports.onOssNormalGet = function(
  success,
  error,
  dataToken,
  endPoint,
  bucket,
  object,
  objectDownLoadKey
) {
  exec(success, error, 'FileUpload', 'onOssNormalGet', [
    dataToken,
    endPoint,
    bucket,
    object,
    objectDownLoadKey
  ]);
};


//取消上传/下载任务
exports.onOssNormalCancel = function(
  success,
  error,
) {
  exec(success, error, 'FileUpload', 'onOssNormalCancel', [

  ]);
};

//图片缩放
exports.onOssImgResize = function(
  success,
  error,
  dataToken,
  endPoint,
  bucket,
  object,
  width,
  height
) {
  exec(success, error, 'FileUpload', 'onOssImgResize', [
    dataToken,
    endPoint,
    bucket,
    object,
    width,
    height
  ]);
};

//图片水印
exports.onOssWatermark = function(
  success,
  error,
  dataToken,
  endPoint,
  bucket,
  object,
  waterMark,
  size
) {
  exec(success, error, 'FileUpload', 'onOssWatermark', [
    dataToken,
    endPoint,
    bucket,
    object,
    waterMark,
    size
  ]);
};