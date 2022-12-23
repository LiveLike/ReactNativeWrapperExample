import EngagementSDK



@objc(Livelike)
class Livelike: RCTEventEmitter {
    @objc
    override func supportedEvents() -> [String]! {
        return ["onMessageReceived", "onMessageDeleted", "onWidgetReceived", "onPollVotesChange", "onDebugLog"];
    }
    
    var accessToken: String = ""
        var currentAccessToken: String?
    var currentQuizWidgetModel: QuizWidgetModel?
    var currentPollWidgetModel: PollWidgetModel?
    var currentWidget: Widget?
    
    
  struct DataProvider {
      static var shared = DataProvider()
      var engagementSDK: EngagementSDK!
      var contentSession: ContentSession?
      var chatSession: ChatSession?
      var producerChatSession: ChatSession?
      private init() {
      }
    }
  
    override init() {
        super.init()
    }
    
    var debugLogSequence = 0
    var debugLogIsEnabled = true
    func debugLog(message: String, data: Any? = nil) {
        if (!debugLogIsEnabled) { return }
        debugLogSequence += 1
        let timestamp = UInt64(Date().timeIntervalSince1970 * 1000000000)
        self.sendEvent(
            withName:"onDebugLog",
            body: [
                 "timestamp": "\(timestamp)",
                 "sequence": "\(timestamp)ios\(debugLogSequence)",
                 "message": message,
                 "data": data
                 ]
        );
    }
    
    @objc(setIsDebugLoggingEnabled:)
    func setIsDebugLoggingEnabled(isEnabled: Bool) -> Void {
        self.debugLogIsEnabled = isEnabled
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
        debugLog(message: "startContentSession \(programID)")
        let config = SessionConfiguration(programID: programID)
        DataProvider.shared.contentSession = DataProvider.shared.engagementSDK.contentSession(config: config)
        DataProvider.shared.contentSession?.delegate = self
        resolve(true)
    }
    
    @objc(withResolver:withRejecter:)
    func closeContentSession(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
      DataProvider.shared.contentSession?.close()
        resolve(true)
    }
    
    @objc(joinProducerChatRoom:withResolver:withRejecter:)
    func joinChatRoom(roomID: String, resolve:@escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        debugLog(message: "joinProducerChatRoom \(roomID)")
        let config = ChatSessionConfig(roomID: roomID)
        debugLog(message: "connectChatRoom")
        DataProvider.shared.engagementSDK.connectChatRoom(config: config) { [weak self] result in
              guard let self = self else { return }
                switch result {
                case .success(let chatSession):
                    self.debugLog(message: "connectChatRoom success")
                    DataProvider.shared.producerChatSession = chatSession
                    DataProvider.shared.producerChatSession?.addDelegate(self)
                    print("joined producer chat room \(chatSession.roomID)")
                    resolve(chatSession.roomID)
                case .failure(let error):
                    self.debugLog(message: "connectChatRoom error: \(error)")
                    print("we have an error \(error)")
                }
            }
    }
    
    @objc(joinChatRoom:avatarURL:withResolver:withRejecter:)
    func joinChatRoom(roomID: String, avatarURL: String?, resolve:@escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        DataProvider.shared.engagementSDK.createUserChatRoomMembership(roomID: roomID) {
            result in
            switch result {
            case .success(let member):
                print("Created chatroom membership: \(member)")
            case .failure(let error):
                print("Failed to create chatroom membership: \(error)")
            }
        }
        let config = ChatSessionConfig(roomID: roomID)
        DataProvider.shared.engagementSDK.connectChatRoom(config: config) { [weak self] result in
              guard let self = self else { return }
                switch result {
                case .success(let chatSession):
                    DataProvider.shared.chatSession = chatSession
                    if let avatarURL = avatarURL {
                        DataProvider.shared.chatSession?.avatarURL = URL(string: avatarURL)
                    }
                    DataProvider.shared.chatSession?.addDelegate(self)
                    self.sendOnMessageReceivedEvent(messages: chatSession.messages, roomID: chatSession.roomID)
                    print("joined chat room \(chatSession.roomID)")
                    resolve(chatSession.roomID)
                case .failure(let error):
                    print("we have an error \(error)")
                }
            }
    }
    
    @objc(getResults:withResolver:withRejecter:)
    func getResults(widgetID: String,resolve:@escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        var totalVoteCount: Int?;
        var options: Any?;
        switch currentWidget?.kind.displayName {
        case "Text Quiz":
            let choicesWithCount = currentQuizWidgetModel?.choices.map {$0.answerCount}
            totalVoteCount = currentQuizWidgetModel?.choices.map { $0.answerCount ?? 0 }.reduce(0, +)
            options = currentQuizWidgetModel?.choices.map {["id": $0.id, "answerCount": $0.answerCount, "isCorrect": $0.isCorrect]}
        case "Text Poll":
            totalVoteCount = currentPollWidgetModel?.options.map { $0.voteCount ?? 0 }.reduce(0, +)
            options = currentPollWidgetModel?.options.map {["id": $0.id, "answerCount": $0.voteCount]}
        default:
            break;
        }
        resolve(["totalVoteCount": totalVoteCount, "options": options])
    }
    
    @objc(getProgramLeaderboards:withResolver:withRejecter:)
    func getProgramLeaderboards(programID: String, resolve: @escaping RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        DataProvider.shared.engagementSDK.getLeaderboards(programID: programID) { result in
            switch result {
            case .success(let leaderboards):
                let leaderboardDicts: NSMutableArray = []
                for leaderboard in leaderboards {
                    let rewardDict: NSMutableDictionary = [:]
                    rewardDict["id"] = leaderboard.rewardItem.id
                    rewardDict["name"] = leaderboard.rewardItem.name
                    
                    let leaderboardDict: NSMutableDictionary = [:]
                    leaderboardDict["id"] = leaderboard.id
                    leaderboardDict["name"] = leaderboard.name
                    leaderboardDict["rewardItem"] = rewardDict
                    
                    leaderboardDicts.add(leaderboardDict)
                }
                resolve(leaderboardDicts)
            case .failure(let error):
                print("error \(error)")
            }
        }
    }
    
    @objc(lockInAnswer:choiceID:withResolver:withRejecter:)
    func lockInAnswer(widgetID: String, choiceID: String, resolve:@escaping RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        currentQuizWidgetModel?.lockInAnswer(choiceID: choiceID) { result in
            switch result {
            case .success(_):
                resolve(true)
            case .failure(let error):
                print("error \(error)")
            }
        }
    }
    
    @objc(submitVote:choiceID:withResolver:withRejecter:)
    func submitVote(widgetID: String, choiceID: String, resolve:@escaping RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        currentPollWidgetModel?.delegate = self
        currentPollWidgetModel?.submitVote(optionID: choiceID, completion: { result in
            switch result {
            case .success(_):
                print("vote submitted successfully")
                resolve(true)
            case .failure(let error):
                print("error \(error)")
            }
        })
    }
    
    @objc(sendMessage:)
    func sendMessage(message: String) -> Void {
      guard let chatSession = DataProvider.shared.chatSession else {
            return
        }
        let textMessage = NewChatMessage(text: message)
      DataProvider.shared.chatSession?.sendMessage(textMessage) { result in
            switch result {
            case .success(let chatMessage):
                print("Chat Message ID: \(chatMessage.id) successfuly sent")
                // did send message callback
            case .failure(let error):
                print(error.localizedDescription)
            }
        }
    }
    
    @objc(sendEvent:)
    func sendEvent(event: String) -> Void {
      guard let producerChatSession = DataProvider.shared.producerChatSession else {
            return
        }
      DataProvider.shared.producerChatSession?.sendCustomMessage(event) { result in
            switch result {
            case .success(let chatMessage):
                print("Event Message ID: \(chatMessage.id) successfuly sent")
                // did send message callback
            case .failure(let error):
                print(error.localizedDescription)
            }
        }
    }
    
    func sendOnMessageReceivedEvent(
        messages: [ChatMessage],
        roomID: String
    ) {
        let filteredMessages = messages.filter { $0.id.asString != "" }
        let transformedMessages = filteredMessages.map {[
            "id": $0.id.asString,
            "text": $0.text,
            "photoURL": $0.profileImageURL?.absoluteString,
            "customData": $0.customData,
            "senderNickname": $0.senderNickname,
            "senderID": $0.senderID,
            "isMine": $0.isMine,
            "timestamp": $0.timestamp.description
        ]}
        if transformedMessages.isEmpty == false {
            self.sendEvent(
                withName:"onMessageReceived",
                body: [
                    "roomID": roomID,
                    "messages": transformedMessages
                ]
            );
        }
    }
    
    func sendOnMessageDeletedEvent(messageID: String) {
        self.sendEvent(
            withName:"onMessageDeleted",
            body: messageID
        );
    }
    
    func sendOnWidgetReceivedEvent(widget: QuizWidgetModel) {
        self.sendEvent(
            withName:"onWidgetReceived",
            body: [
                "id": widget.id,
                "kind": "text-quiz",
                "widgetTitle": widget.question,
                "createdAt": widget.createdAt.rawValue,
                "publishedAt": widget.publishedAt?.rawValue,
                "interactionTimeInterval": widget.interactionTimeInterval,
                "customData": widget.customData ?? "",
                "options": widget.choices.map {[
                    "id": $0.id,
                    "text": $0.text,
                    "imageUrl": $0.imageURL ?? "",
                    "isCorrect": $0.isCorrect,
                    "voteCount": $0.answerCount,
                ]} ?? []
            ]
        );
        
    }
    
    func sendOnWidgetReceivedEvent(widget: PollWidgetModel) {
        self.sendEvent(
            withName:"onWidgetReceived",
            body: [
                "id": widget.id,
                "kind": "text-poll",
                "widgetTitle": widget.question,
                "createdAt": widget.createdAt.rawValue,
                "publishedAt": widget.publishedAt?.rawValue,
                "interactionTimeInterval": widget.interactionTimeInterval,
                "customData": widget.customData ?? "",
                "options": widget.options.map {[
                    "id": $0.id,
                    "text": $0.text,
                    "imageUrl": $0.imageURL ?? "",
                    "isCorrect": false,
                    "voteCount": $0.voteCount,
                ]} ?? []
            ]
        );
    }
    
    func sendOnPollVotesChangeEvent(pollModel: PollWidgetModel, voteCount: Int, optionID: String) {
        let options = pollModel.options.map {["id": $0.id, "answerCount": $0.voteCount]}
        let totalVoteCount = currentPollWidgetModel?.options.map { $0.voteCount ?? 0 }.reduce(0, +)
        self.sendEvent(withName: "onPollVotesChange" , body: ["options": options, "totalVoteCount": totalVoteCount])
    }
}

extension Livelike: ChatSessionDelegate {
    public func chatSession(_ chatSession: ChatSession, didRecieveNewMessage message: ChatMessage) {
        print(">> MSG RECEIVED SWIFT", message.id, message.text ?? "")
        debugLog(message: "didRecieveNewMessage", data: [
            "message": message.text ?? "",
            "timestamp": message.timestamp.rawValue,
            "createdAt": message.createdAt.description,
            "custom_data": message.customData?.description ?? ""
            ])
        sendOnMessageReceivedEvent(messages: [message], roomID: chatSession.roomID)
    }
    
    func chatSession(_ chatSession: ChatSession, didRecieveRoomUpdate chatRoom: ChatRoomInfo) {}
    
    func chatSession(_ chatSession: ChatSession, didPinMessage message: PinMessageInfo) {}
    
    func chatSession(_ chatSession: ChatSession, didUnpinMessage pinMessageInfoID: String) {}
    
    func chatSession(_ chatSession: ChatSession, didRecieveMessageUpdate message: ChatMessage) {}
    
    func chatSession(_ chatSession: ChatSession, didDeleteMessage messageID: ChatMessageID) {
        print(">> MSG DELETED SWIFT", messageID.asString)
        sendOnMessageDeletedEvent(messageID: messageID.asString)
    }
}


extension Livelike: ContentSessionDelegate {
    func playheadTimeSource(_ session: ContentSession) -> Date? {
        return nil
    }
    
    func session(_ session: ContentSession, didChangeStatus status: SessionStatus) {
        print("change status \(status)")
    }
    
    func session(_ session: ContentSession, didReceiveError error: Error) {
        print("error \(error)")
    }
    
    func chat(session: ContentSession, roomID: String, newMessage message: ChatMessage) {
        print("message \(message)")
    }
    
    func contentSession(_ session: ContentSession, didReceiveWidget widgetModel: WidgetModel) {
        switch widgetModel {
        case .quiz(let quizModel):
           debugLog(message: "didReceiveWidget", data: [
                    "question": quizModel.question,
                    "createdAt": quizModel.createdAt.description,
                    "publishedAt": quizModel.publishedAt?.description
                ])
            currentQuizWidgetModel = quizModel
            sendOnWidgetReceivedEvent(widget: quizModel)
        case .poll(let pollModel):
           debugLog(message: "didReceiveWidget", data: [
                    "question": pollModel.question,
                    "createdAt": pollModel.createdAt.description,
                    "publishedAt": pollModel.publishedAt?.description
                ])
            currentPollWidgetModel = pollModel
            sendOnWidgetReceivedEvent(widget: pollModel)
        default:
            print("do nothing")
        }
    }
    
    func widget(_ session: ContentSession, didBecomeReady widget: Widget) {
        currentWidget = widget
    }
}

extension Livelike: PollWidgetModelDelegate {
    func pollWidgetModel(_ model: PollWidgetModel, voteCountDidChange voteCount: Int, forOption optionID: String) {
        sendOnPollVotesChangeEvent(pollModel: model, voteCount: voteCount, optionID: optionID)
    }
}

extension Livelike: AccessTokenStorage {
    func fetchAccessToken() -> String? {
        return self.currentAccessToken
    }
    func storeAccessToken(accessToken: String) {
        self.currentAccessToken = accessToken
    }
}

