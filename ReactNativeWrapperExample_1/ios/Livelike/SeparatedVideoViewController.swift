import AVKit
import EngagementSDK
import UIKit

class CustomWidgetTimeline: InteractiveWidgetTimelineViewController {
   
  var themeFromJson: Theme? = nil
  var isNoWidgetViewUp: Bool = false
  
  override init(contentSession:ContentSession) {
      
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
      super.init(contentSession: contentSession)
    }
  
  open override func didReceiveNewWidget(_ widgetModel: WidgetModel) -> WidgetModel? {
    if(isNoWidgetViewUp){
      Livelike.shared?.sendEventToRN(event:"hideNoWidgetView")
      self.isNoWidgetViewUp = false
    }
    return widgetModel
  }
  
  override func didLoadInitialWidgets(_ widgetModels: [WidgetModel]) -> [WidgetModel] {
    
    if(widgetModels.count == 0){
      Livelike.shared?.sendEventToRN(event:"showNoWidgetView")
      self.isNoWidgetViewUp = true
    }
    return super.didLoadInitialWidgets(widgetModels)
  }
  
  override func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
      return 20
  }
  
  override func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
    tableView.backgroundColor = .gray
    let separatorView = UIView(frame: CGRect(x: 0, y: 0, width: view.frame.width, height: 20))
    return separatorView
   }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
    override func makeWidget(_ widgetModel: WidgetModel) -> UIViewController? {
        let widget = super.makeWidget(widgetModel) as? Widget
        widget?.theme = self.themeFromJson ?? Theme()
        return widget
    }
}

class SeparatedVideoViewController: UIViewController {
  
  // MARK: EngagementSDK Properties
  private lazy var timelineVC: CustomWidgetTimeline = {
    let vc = CustomWidgetTimeline(contentSession: Livelike.DataProvider.shared.contentSession!)
    vc.view.translatesAutoresizingMaskIntoConstraints = false
    return vc
  }()
  
  init() {
    super.init(nibName: nil, bundle: nil)
  }
  
  
  // MARK: - UI Elements
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setContentSession() {
    setUpEngagementSDKLayout()
  }
  
  // MARK: - UIViewController Life Cycle
  override func viewDidLoad() {
    super.viewDidLoad()
    
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


extension SeparatedVideoViewController {
  private func setUpEngagementSDKLayout() {
    timelineVC.view.backgroundColor = .gray
    addChild(timelineVC)
    timelineVC.didMove(toParent: self)
    view.addSubview(timelineVC.view)
    timelineVC.view.backgroundColor = .gray

  
    NSLayoutConstraint.activate([
        timelineVC.view.topAnchor.constraint(equalTo: view.topAnchor),
        timelineVC.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
        timelineVC.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
        timelineVC.view.heightAnchor.constraint(equalTo: view.heightAnchor),
    ])
  }
  
}

