import EngagementSDK
import React



@objc(Livelike)
class Livelike: RCTEventEmitter, AccessTokenStorage {
  
  func fetchAccessToken() -> String? {
      let token = UserDefaults.standard.string(forKey: "UserDefaultsKeys.liveLikeUserAccessToken")
              return token
      }
      
      func storeAccessToken(accessToken: String) {
          // store access token
          UserDefaults.standard.set(accessToken, forKey: "UserDefaultsKeys.liveLikeUserAccessToken")
                  UserDefaults.standard.synchronize()
      }
  
    @objc
    override func supportedEvents() -> [String]! {
        return ["onMessageReceived", "onMessageDeleted", "onWidgetReceived", "onPollVotesChange", "onDebugLog"];
    }
    
    
    var accessToken: String = ""
    var currentAccessToken: String?
    
  struct DataProvider {
    static var shared = DataProvider()
    var engagementSDK: EngagementSDK!
    var contentSession: ContentSession?
    private init() {
    }
  }
  
    override init() {
        super.init()
    }
    

    @objc(initialize:accessToken:withResolver:withRejecter:)
    func initialize(clientID: String, accessToken: String?, resolve:@escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        self.currentAccessToken = accessToken
        
        var sdkConfig = EngagementSDKConfig(clientID: clientID)
        sdkConfig.accessTokenStorage = self
        
      DataProvider.shared.engagementSDK = EngagementSDK(config: sdkConfig)
      DataProvider.shared.engagementSDK.getCurrentUserProfileID { result in
            switch result {
            case .success(let profileID):
                resolve(["accessToken": self.currentAccessToken, "profileID": profileID])
                
            case .failure(let error):
                print("we have an error \(error)")
            }
        }
    }
    
    @objc(startContentSession:withResolver:withRejecter:)
    func startContentSession(programID: String, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        let config = SessionConfiguration(programID: programID)
        DataProvider.shared.contentSession = DataProvider.shared.engagementSDK?.contentSession(config: config)
        resolve(true)
    }
    
    @objc(withResolver:withRejecter:)
    func closeContentSession(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        DataProvider.shared.contentSession?.close()
        DataProvider.shared.contentSession = nil
        resolve(true)
    }
  
}


