/********* FileUpload.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <AliyunOSSiOS/OSSService.h>
#import "OSSManager.h"
#import "OSSWrapper.h"

@interface FileUpload : CDVPlugin {
    NSString *uploadFilePath;//上传文件的路径
    NSString *objectKey;     //对象名字
    OSSWrapper *oss;
    NSString *mkey;
    NSString *endPoint;
    
    //@property (nonatomic, strong) OSSWrapper *oss;

}

- (void)putObject:(CDVInvokedUrlCommand*)command;
@end

@implementation FileUpload

- (void)pluginInitialize {
    CDVViewController *viewController = (CDVViewController *)self.viewController;
    mkey = [viewController.settings objectForKey:@"SECRET_KEY"];//获取插件的SECRET_KEY
}


//普通上传
- (void)putObject:(CDVInvokedUrlCommand*)command
{ 
	[self pluginInitialize];
    CDVPluginResult* pluginResult = nil;
    NSString* str_data = [command.arguments objectAtIndex:0]; 
    endPoint =  [command.arguments objectAtIndex:1];
    NSString* bucket =    [command.arguments objectAtIndex:2];
    objectKey =  [command.arguments objectAtIndex:3];
    uploadFilePath = [command.arguments objectAtIndex:4];
    [self initDefaultClient:str_data]; 
    [oss asyncPutImage:objectKey localFilePath:uploadFilePath success:^(NSString *result) {
      //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
        OSSLogDebug(@"上传文件 OK");
    } failure:^(NSError *error) { 
      //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        OSSLogDebug(@"上传文件 error");
    }];   

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

 - (void) initDefaultClient:(NSString*)str_data {
    
     // 针对只有一个region下bucket的数据上传下载操作时,可以将client实例给App单例持有。
    id<OSSCredentialProvider> credentialProvider = [[OSSAuthCredentialProvider alloc] initWithAuthServerUrl:str_data  mkey:mkey];
    OSSClientConfiguration *cfg = [[OSSClientConfiguration alloc] init];
    cfg.maxRetryCount = 3;
    cfg.timeoutIntervalForRequest = 15;
    cfg.isHttpdnsEnable = NO;
    cfg.crc64Verifiable = YES;
    
    OSSClient *defaultClient = [[OSSClient alloc] initWithEndpoint:endPoint credentialProvider:credentialProvider clientConfiguration:cfg];
    [OSSManager sharedManager].defaultClient = defaultClient;
  
 }
  




@end
