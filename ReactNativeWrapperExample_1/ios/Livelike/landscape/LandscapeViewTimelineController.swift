import AVKit
import EngagementSDK
import UIKit

class LandscapeTimelineViewController: UIViewController, ContentSessionDelegate {
  func playheadTimeSource(_ session: ContentSession) -> Date? {
      return Date()
  }
  
  func widget(_ session: ContentSession, didBecomeReady widget: Widget) {
      print("*** didBecomeReady")
  }
  
  func session(_ session: ContentSession, didChangeStatus status: SessionStatus) {
    
  }
  
  func session(_ session: ContentSession, didReceiveError error: Error) {
    
  }
  
  func chat(session: ContentSession, roomID: String, newMessage message: ChatMessage) {
    
  }
  
  func contentSession(_ session: ContentSession, didReceiveWidget widget: WidgetModel) {
    
  }
  
  
  private let  interactiveTimelineWidgetViewDelegate = InteractiveTimelineWidgetViewDelegate()
  private var correctWidget: Widget? = nil
  private var widgetToggle: Bool = false
  private let widgetContainer: UIView = {
      let widgetContainer = UIView()
      widgetContainer.translatesAutoresizingMaskIntoConstraints = false
      return widgetContainer
  }()
  
  
  init() {
    super.init(nibName: nil, bundle: nil)
  }
  
  
  // MARK: - UI Elements
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setContentSession() {
    findLastPostedLandscapeWidgetInFirstPage()
    //addListenerForNewWidgets()
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
          }
        }
        
      case let .failure(error):
        print("Error While loading widgets: \(error.localizedDescription)")
      }
    }
  }
  
  func presentCurrentWidget(){
    DispatchQueue.main.async {
      if self.correctWidget != nil {
        self.addChild(self.correctWidget!)
        self.widgetContainer.addSubview(self.correctWidget!.view)
        self.correctWidget!.didMove(toParent: self)
        self.correctWidget!.delegate = self.interactiveTimelineWidgetViewDelegate
        self.correctWidget?.moveToNextState()
        
        NSLayoutConstraint.activate([
          self.correctWidget!.view.topAnchor.constraint(equalTo: self.widgetContainer.topAnchor),
          self.correctWidget!.view.leadingAnchor.constraint(equalTo: self.widgetContainer.leadingAnchor),
          self.correctWidget!.view.trailingAnchor.constraint(equalTo: self.widgetContainer.trailingAnchor),
          self.correctWidget!.view.heightAnchor.constraint(lessThanOrEqualTo: self.widgetContainer.heightAnchor)
        ])
      }
    }
  }
  
  func toggleWidgetVisibility(){
    if(widgetToggle){
      self.presentCurrentWidget()
    } else{
      DispatchQueue.main.async {
        self.correctWidget?.view.removeFromSuperview()
        self.correctWidget?.removeFromParent()
      }
    }
    widgetToggle.toggle()
  }
  
  func findWidgetWithLandscape(widgets: [Widget]) -> Widget? {
    for widget in widgets {
      if let widgetModel = widget.widgetModel {
        switch widgetModel {
        case .alert(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .cheerMeter(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .quiz(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .prediction(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .predictionFollowUp(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .poll(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .imageSlider(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .socialEmbed(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        case .videoAlert(let model):
          if model.widgetAttributes.first(where: {$0.key == "landscape" && $0.value == "true"}) != nil {
            return widget
          }
          
        default:
          break
        }
      }
    }
    return nil
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
  
  
  // MARK: - UIViewController Life Cycle
  override func viewDidLoad() {
    super.viewDidLoad()
    view.addSubview(widgetContainer)
    NSLayoutConstraint.activate([
      widgetContainer.centerYAnchor.constraint(equalTo: view.centerYAnchor)])
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
  

