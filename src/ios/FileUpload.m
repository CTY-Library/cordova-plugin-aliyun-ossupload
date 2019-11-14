/********* FileUpload.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AliyunOSSiOS/OSSService.h>
 

@interface FileUpload : CDVPlugin {
    NSString *uploadFilePath;//上传文件的路径
    NSString *objectKey;     //对象名字
    OSSWrapper *oss;
    NSString *mkey; 
}

- (void)putObject:(CDVInvokedUrlCommand*)command;
@end

@implementation FileUpload

- (void)pluginInitialize {
    CDVViewController *viewController = (CDVViewController *)self.viewController;
    self.mkey = [viewController.settings objectForKey:@"SECRET_KEY"];//获取插件的SECRET_KEY
}


//普通上传
- (void)putObject:(CDVInvokedUrlCommand*)command
{ 
    CDVPluginResult* pluginResult = nil;
    NSString* str_data = [command.arguments objectAtIndex:0]; 
    NSString* endPoint =  [command.arguments objectAtIndex:1];
    NSString* bucket =    [command.arguments objectAtIndex:2];
    self.objectKey =    [command.arguments objectAtIndex:3];
    self.uploadFilePath = [command.arguments objectAtIndex:4];
    [self initDefaultClient:str_data]; 
    [self.oss asyncPutImage:self.objectKey localFilePath:self.uploadFilePath success:^(NSString *result) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
    } failure:^(NSError *error) { 
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
    }];   

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

 - (void) initDefaultClient:(NSString*)str_data {
    
     // 针对只有一个region下bucket的数据上传下载操作时,可以将client实例给App单例持有。
    NSString* _oss_STSTOKEN_URL = [command.arguments objectAtIndex:0];
    id<OSSCredentialProvider> credentialProvider = [[OSSAuthCredentialProvider alloc] initWithAuthServerUrl:str_data,mkey:self.mkey];
    OSSClientConfiguration *cfg = [[OSSClientConfiguration alloc] init];
    cfg.maxRetryCount = 3;
    cfg.timeoutIntervalForRequest = 15;
    cfg.isHttpdnsEnable = NO;
    cfg.crc64Verifiable = YES;
    
    OSSClient *defaultClient = [[OSSClient alloc] initWithEndpoint:OSS_ENDPOINT credentialProvider:credentialProvider clientConfiguration:cfg];
    [OSSManager sharedManager].defaultClient = defaultClient;
 
 }
  




@end
