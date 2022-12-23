#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTEventDispatcher.h>

@interface RCT_EXTERN_MODULE(Livelike, RCTEventEmitter)

RCT_EXTERN_METHOD(setIsDebugLoggingEnabled:(BOOL)isEnabled)

RCT_EXTERN_METHOD(initialize:(NSString)clientID
                  accessToken:(NSString)accessToken
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startContentSession:(NSString)programID
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(joinProducerChatRoom:(NSString)roomID
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(joinChatRoom:(NSString)roomID
                  avatarURL:(NSString)avatarURL
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(sendMessage:(NSString)message)

RCT_EXTERN_METHOD(sendEvent:(NSString)event)

RCT_EXTERN_METHOD(getResults:(NSString)widgetID
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
                  
RCT_EXTERN_METHOD(getProgramLeaderboards:(NSString)programID
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(lockInAnswer:(NSString)widgetID
                  choiceID:(NSString)choiceID
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(submitVote:(NSString)widgetID
                  choiceID:(NSString)choiceID
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

- (NSArray<NSString *> *)supportedEvents {
  return @[@"onMessageReceived", @"onMessageDeleted", @"onWidgetReceived", @"onPollVotesChange", @"onDebugLog"];
}
+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
