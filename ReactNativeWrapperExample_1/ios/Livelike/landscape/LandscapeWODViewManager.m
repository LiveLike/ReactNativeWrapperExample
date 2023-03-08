//
//  LiveLikeiOSViewManager.m
//  ReactLivelike
//
//  Created by Changdeo Jadhav on 16/01/22.
//

#import "React/RCTViewManager.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTEventDispatcher.h>

@interface RCT_EXTERN_MODULE(LandscapeWODViewManager, RCTViewManager)
RCT_EXPORT_VIEW_PROPERTY(programId, NSString)
RCT_EXPORT_VIEW_PROPERTY(toggleWidget, BOOL)
@end
