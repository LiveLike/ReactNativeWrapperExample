//
//  LiveLikeiOSViewManager.swift
//  ReactLivelike
//
//  Created by Changdeo Jadhav on 16/01/22.
//

import Foundation
import UIKit
import React

@objc(LandscapeWODViewManager)
class LandscapeWODViewManager: RCTViewManager {
  override func view() -> UIView! {
    return LandscapeWODView()
  }
  override static func requiresMainQueueSetup() -> Bool {
     return true
   }
}
