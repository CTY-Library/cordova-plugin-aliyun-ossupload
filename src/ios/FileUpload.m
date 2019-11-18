/********* FileUpload.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AliyunOSSiOS/OSSService.h>
#import "OSSManager.h"
#import "OSSWrapper.h"

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonCrypto.h>

  //  @property (nonatomic, strong) OSSWrapper *oss;

@interface FileUpload : CDVPlugin {
    NSString *uploadFilePath;//上传文件的路径
    NSString *objectKey;     //对象名字
    NSString *mkey;
    NSString *endPoint;
    
    CDVPluginResult* pluginResult;
}

    @property (nonatomic, strong) OSSWrapper *oss;

- (void)putObject:(CDVInvokedUrlCommand*)command;
@end

@implementation FileUpload

- (void)pluginInitialize {
    CDVViewController *viewController = (CDVViewController *)self.viewController;
    mkey = [viewController.settings objectForKey:@"secret_key"];//获取插件的SECRET_KEY
    [self setupOSS];
    //[OSSLog enableLog];     // 开启sdk的日志功能
}
 

// 普通下载
- (void)onOssNormalGet:(CDVInvokedUrlCommand *)command
{
    NSString* objectKey = [command.arguments objectAtIndex:0];
    NSString* bucket =    [command.arguments objectAtIndex:1];
    NSString* objectDownLoadKey =    [command.arguments objectAtIndex:2];

    [self.oss asyncGetImage:objectKey objectDownLoadKey:objectDownLoadKey oss_bucket_private:bucket success:^(id result) {
       //返回结果
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:^(NSError *error) {
        //返回结果
         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

//普通上传
- (void)onOssNormalPut:(CDVInvokedUrlCommand*)command
{
    NSString* str_data = [command.arguments objectAtIndex:0]; 
    endPoint =  [command.arguments objectAtIndex:1];
    NSString* bucket =    [command.arguments objectAtIndex:2];
    objectKey =  [command.arguments objectAtIndex:3];
    uploadFilePath = [command.arguments objectAtIndex:4];
    
    [self initDefaultClient:str_data];//初始化连接
    
    [self.oss asyncPutImage: objectKey localFilePath:uploadFilePath oss_bucket_private:bucket success:^(id result) {
        //返回结果
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
       //OSSLogDebug(@"上传文件 OK");
     } failure:^(NSError *error) {
       //返回结果
       pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
       [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
       //OSSLogDebug(@"上传文件 error");
     }];
}

// 图片缩放
- (void)onOssImgResize:(CDVInvokedUrlCommand *)command {

    NSString* objectKey = [command.arguments objectAtIndex:0];
    NSString* bucket =    [command.arguments objectAtIndex:1];
    NSString* str_width = [command.arguments objectAtIndex:2];
    NSString* str_height =    [command.arguments objectAtIndex:3];
    int width = [str_width intValue];
    int height = [str_height intValue];
    
    [self.oss reSize:objectKey oss_bucket_private:bucket picWidth:width picHeight:height success:^(id result) {
        NSString *filePath = (NSString *)result;
        //返回结果
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: filePath];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:^(NSError *error) {
        //返回结果
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

// 图片水印
- (void)onOssWatermark:(CDVInvokedUrlCommand *)command {
    NSString* objectKey = [command.arguments objectAtIndex:0];
    NSString* bucket =    [command.arguments objectAtIndex:1];
    NSString * waterMark =  [command.arguments objectAtIndex:2];
    NSString* str_size =    [command.arguments objectAtIndex:3];
    int size = [str_size intValue];
   
    [self.oss textWaterMark:objectKey oss_bucket_private:bucket waterText:waterMark objectSize:size success:^(id result) {
        //返回结果
        NSString *filePath = (NSString *)result;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: filePath];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     } failure:^(NSError *error) {
        //返回结果
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

// 取消普通上传/下载任务
- (void)onOssNormalCancel:(CDVInvokedUrlCommand *)command {
     [self.oss normalRequestCancel];
     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
     [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setupOSS {
    _oss = [[OSSWrapper alloc] init];
}

 - (void) initDefaultClient:(NSString*)str_data {
    NSData *data = [self aes256DecryptWithString:str_data key:mkey];
     //NSString* tt = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
     NSDictionary * object = [NSJSONSerialization JSONObjectWithData:data
                                                                    options:kNilOptions
                                                                      error:nil];
    int statusCode = [[object objectForKey:@"StatusCode"] intValue];
    if (statusCode == 200) {
        NSString* AccessKeyId = [object objectForKey:@"AccessKeyId"];
        NSString* AccessKeySecret = [object objectForKey:@"AccessKeySecret"];
        NSString* SecurityToken = [object objectForKey:@"SecurityToken"];
        NSString* Expiration = [object objectForKey:@"Expiration"];
        // 针对只有一个region下bucket的数据上传下载操作时,可以将client实例给App单例持有。
        id<OSSCredentialProvider> credentialProvider = [[OSSAuthCredentialProvider alloc] initWithAuthServerUrl:AccessKeyId  AccessKeySecret:AccessKeySecret SecurityToken:SecurityToken Expiration:Expiration];
        OSSClientConfiguration *cfg = [[OSSClientConfiguration alloc] init];
        cfg.maxRetryCount = 3;
        cfg.timeoutIntervalForRequest = 15;
        cfg.isHttpdnsEnable = NO;
        cfg.crc64Verifiable = YES;
        
        OSSClient *defaultClient = [[OSSClient alloc] initWithEndpoint:endPoint credentialProvider:credentialProvider clientConfiguration:cfg];
        [OSSManager sharedManager].defaultClient = defaultClient;
    } 
 }

 //解密
- (NSData*)aes256DecryptWithString:(NSString*)encryptText key:(NSString *)key{
    if (!key ) {
        NSLog(@"key aes error");
        return nil;
    }
      NSData *data = [[NSData alloc] initWithBase64EncodedString:encryptText options:NSDataBase64DecodingIgnoreUnknownCharacters];//base64解码
    
    NSData *decryptedData = [self aes256DecryptWithData:data key:key];
    return decryptedData;
}

- (NSData *)aes256DecryptWithData:(NSData *)data key:(NSString *)key{
    
    char keyPtr[kCCKeySizeAES256+1];
    bzero(keyPtr, sizeof(keyPtr));
    [key getCString:keyPtr maxLength:sizeof(keyPtr) encoding:NSUTF8StringEncoding];
    NSUInteger dataLength = data.length;
    size_t bufferSize = dataLength + kCCBlockSizeAES128+16;
    void *buffer = malloc(bufferSize);
    size_t numBytesdecrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt(kCCDecrypt, kCCAlgorithmAES128,
                                          kCCOptionPKCS7Padding | kCCOptionECBMode,
                                          keyPtr, kCCBlockSizeAES128+16,
                                          NULL,
                                          data.bytes, dataLength,
                                          buffer, bufferSize,
                                          &numBytesdecrypted);
    if (cryptStatus == kCCSuccess) {
        return [NSData dataWithBytesNoCopy:buffer length:numBytesdecrypted];
    }
    free(buffer);
    return nil;
}
  




@end
