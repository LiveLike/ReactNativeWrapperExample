import AVKit
import EngagementSDK
import UIKit

class LandscapeTimelineViewController: UIViewController, ContentSessionDelegate {
  
  func playheadTimeSource(_ session: ContentSession) -> Date? {
      return Date()
  }
  
  func session(_ session: ContentSession, didChangeStatus status: SessionStatus) {
    
  }
  
  func session(_ session: ContentSession, didReceiveError error: Error) {
    
  }
  
  func chat(session: ContentSession, roomID: String, newMessage message: ChatMessage) {
    
  }
  
  func contentSession(_ session: ContentSession, didReceiveWidget widget: WidgetModel) {
    if self.isValid(widget: widget){
      DispatchQueue.main.async { [weak self] in
        let nextWidget = DefaultWidgetFactory.makeWidget(from: widget)
        if let themeFromJson = self?.themeFromJson{
          nextWidget?.theme = themeFromJson
        }
        self?.hideCurrentWidget()
        self?.correctWidget = nextWidget
        self?.presentCurrentWidget()
      }
    }
    
  }
  
  
  var themeFromJson: Theme? = nil
  init() {
    if let path = Bundle.main.path(forResource: "livelike_styles", ofType: "json") {
      do {
        let jsonData = try Data(contentsOf: URL(fileURLWithPath: path), options: .mappedIfSafe)
        let jsonObject = try JSONSerialization.jsonObject(with: jsonData, options: [])
        themeFromJson = try! Theme.create(fromJSONObject: jsonObject)
      } catch {
        // handle error
        print("Unable to create theme")
      }
    }
    super.init(nibName: nil, bundle: nil)
  }


  required init?(coder aDecoder: NSCoder) {
      super.init(coder: aDecoder)
  }

  
  private let  interactiveTimelineWidgetViewDelegate = InteractiveTimelineWidgetViewDelegate()
  private var correctWidget: Widget? = nil
  private var widgetToggle: Bool = true
  private let queue = DispatchQueue(label: "com.example.myQueue")

  
  func setContentSession() {
    findLastPostedLandscapeWidgetInFirstPage()
    addListenerForNewWidgets()
  }
  
  func addListenerForNewWidgets(){
    Livelike.DataProvider.shared.contentSession?.delegate = self
  }
  
  func findLastPostedLandscapeWidgetInFirstPage(){
    Livelike.DataProvider.shared.contentSession?.getPostedWidgets(page: .first) { [weak self] result in
      switch result {
      case let .success(widgets):
        if let widgets = widgets, !widgets.isEmpty {
          self?.correctWidget = self?.findWidgetWithLandscape(widgets: widgets)
          if self?.correctWidget == nil {
            self?.findLastPostedLandscapeWidgetInNext()
          } else {
            if let themeFromJson = self?.themeFromJson{
              self?.correctWidget?.theme = themeFromJson
            }
          }
        }
        
      case let .failure(error):
        print("Error While loading widgets: \(error.localizedDescription)")
      }
    }
  }
  
  func presentCurrentWidget(){
    guard let correctWidget = self.correctWidget else { return }
    
    DispatchQueue.main.async {
      self.addChild(correctWidget)
      self.view.addSubview(correctWidget.view)
      correctWidget.didMove(toParent: self)
      correctWidget.delegate = self.interactiveTimelineWidgetViewDelegate
      correctWidget.moveToNextState()
      
      correctWidget.view.topAnchor.constraint(equalTo: self.view.topAnchor, constant: 16).isActive = true
      correctWidget.view.leftAnchor.constraint(equalTo: self.view.leftAnchor, constant: 16).isActive = true
      correctWidget.view.rightAnchor.constraint(equalTo: self.view.rightAnchor, constant: -16).isActive = true

    }
  }
  
  func toggleWidgetBool() {
    queue.sync {
      self.widgetToggle.toggle()
    }
  }

  func getWidgetToggleBool() -> Bool {
    var result = false
    queue.sync {
      result = self.widgetToggle
    }
    return result
  }
  
  func hideCurrentWidget() {
      guard let correctWidget = self.correctWidget else { return }
      
      DispatchQueue.main.async {
          correctWidget.view.removeFromSuperview()
          correctWidget.removeFromParent()
      }
  }

  func toggleWidgetVisibility() {
    if self.getWidgetToggleBool() {
          self.presentCurrentWidget()
      } else {
          self.hideCurrentWidget()
      }
      
    self.toggleWidgetBool()
  }
  
  func findWidgetWithLandscape(widgets: [Widget]) -> Widget? {
    for widget in widgets {
      if let widgetModel = widget.widgetModel {
        if(isValid(widget: widgetModel)){
          return widget
        }
      }
    }
    return nil
  }
  
  
  func isValid(widget:WidgetModel)-> Bool{
    switch widget{
    case .alert(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .cheerMeter(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .quiz(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .prediction(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .predictionFollowUp(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .poll(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .imageSlider(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .socialEmbed(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    case .videoAlert(let model):
      if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
        return true
      }
      
    default:
      break
    }
    return false
  }
  
  func findLastPostedLandscapeWidgetInNext(){
    Livelike.DataProvider.shared.contentSession?.getPostedWidgets(page: .next) { [weak self] result in
      switch result {
      case let .success(widgets):
        if let widgets = widgets, !widgets.isEmpty {
          self?.correctWidget = self?.findWidgetWithLandscape(widgets: widgets)
          if self?.correctWidget == nil {
            self?.findLastPostedLandscapeWidgetInNext()
          }
        }
        
      case let .failure(error):
        print("Error While loading widgets: \(error.localizedDescription)")
      }
    }
  }
  
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  //Ending a session
  override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    Livelike.DataProvider.shared.contentSession?.close()
    Livelike.DataProvider.shared.contentSession = nil
  }
}
  

