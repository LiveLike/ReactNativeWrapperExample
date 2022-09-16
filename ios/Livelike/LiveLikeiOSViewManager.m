//
//  LiveLikeiOSViewManager.m
//  ReactLivelike
//
//  Created by Changdeo Jadhav on 16/01/22.
//

#import "React/RCTViewManager.h"
@interface RCT_EXTERN_MODULE(LiveLikeiOSViewManager, RCTViewManager)
  RCT_EXPORT_VIEW_PROPERTY(widgetId, NSString)
  RCT_EXPORT_VIEW_PROPERTY(widgetKind, NSString)
@end

