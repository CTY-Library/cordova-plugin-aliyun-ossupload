var exec = require('cordova/exec');

//注意  
//参数:data 是获取自己应用服务器的OssToken用AES加密后的数据
//参数:object 表示文件名最好加上时间戳

//上传  
exports.onOssNormalPut = function(
  success,
  error,
  data,
  endPoint,
  bucket,
  object,
  localFile
) {
  exec(success, error, 'FileUpload', 'onOssNormalPut', [
    data,
    endPoint,
    bucket,
    object,
    localFile
  ]);
};


//下载
exports.onOssNormalPut = function(
  success,
  error,
  data,
  endPoint,
  bucket,
  object
) {
  exec(success, error, 'FileUpload', 'onOssNormalGet', [
    data,
    endPoint,
    bucket,
    object
  ]);
};


//取消上传/下载任务
exports.onOssNormalPut = function(
  success,
  error,
) {
  exec(success, error, 'FileUpload', 'onOssNormalGet', [

  ]);
};

//图片缩放
exports.onOssNormalPut = function(
  success,
  error,
  data,
  endPoint,
  bucket,
  object,
  width,
  height
) {
  exec(success, error, 'FileUpload', 'onOssNormalGet', [
    data,
    endPoint,
    bucket,
    object,
    width,
    height
  ]);
};

//图片水印
exports.onOssNormalPut = function(
  success,
  error,
  data,
  endPoint,
  bucket,
  object,
  waterMark,
  size
) {
  exec(success, error, 'FileUpload', 'onOssNormalGet', [
    data,
    endPoint,
    bucket,
    object,
    waterMark,
    size
  ]);
};