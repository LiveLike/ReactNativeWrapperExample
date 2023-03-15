#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTEventDispatcher.h>

@interface RCT_EXTERN_MODULE(Livelike, RCTEventEmitter)

RCT_EXTERN_METHOD(initialize:(NSString)clientID
                  accessToken:(NSString)accessToken
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startContentSession:(NSString)programID
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

- (NSArray<NSString *> *)supportedEvents {
  return @[@"showNoWidgetView", @"hideNoWidgetView"];
}
+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
