var exec = require('cordova/exec');

exports.putObject = function(
  success,
  error,
  data,
  endPoint,
  bucket,
  object,
  localFile
) {
  exec(success, error, 'FileUpload', 'putObject', [
    data,
    endPoint,
    bucket,
    object,
    localFile
  ]);
};
