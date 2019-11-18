阿里云文件操作插件


参考链接

https://help.aliyun.com/document_detail/31920.html

https://github.com/aliyun/aliyun-oss-ios-sdk

https://github.com/aliyun/aliyun-oss-android-sdk



安装命令

 ionic cordova plugin add  https://github.com/lounai-chen/cordova-plugins-aliyunOSSupload  --variable SECRETKEY=xxxxxx --save


使用案例关键代码

var _this = this;

var uploadurlfile = _this.temppath.substr(8,_this.temppath2.length - 8); //安卓路径要去掉file:///

alert(uploadurlfile)

this.xxxx.getOSSToken({},false).then(result => {

try{

    aliyunOSSupload.putObject(
    
    (e) => {_this.onSuccess(e)},
    
    (e) =>{_this.onError(e)},
    
    result.data,
    
    'oss-cn-shenzhen.aliyuncs.com',
    
    'xxx',
    
    'mp3_20191115_152830.mp3',
    
     uploadurlfile,
     
    );
    
}catch(e){alert(' error: '+e)}; 

});
