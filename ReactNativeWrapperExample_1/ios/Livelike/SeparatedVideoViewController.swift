import AVKit
import EngagementSDK
import UIKit

class CustomWidgetTimeline: InteractiveWidgetTimelineViewController {
  
//   static let themeFromJson: Theme = {
//       let theme = Theme()
//       if let path = Bundle.main.path(forResource: "livelike_styles", ofType: "json") {
//           do {
//               let jsonData = try Data(contentsOf: URL(fileURLWithPath: path), options: .mappedIfSafe)
//               
//               return try Theme.create(fromJSONObject: jsonData)
//           } catch {
//               // handle error
//           }
//       }
//        return Theme()
//    }()
    
    override func makeWidget(_ widgetModel: WidgetModel) -> UIViewController? {
        let widget = super.makeWidget(widgetModel) as? Widget
        //widget?.theme = CustomWidgetTimeline.themeFromJson
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
    addChild(timelineVC)
    timelineVC.didMove(toParent: self)
    view.addSubview(timelineVC.view)

  
    NSLayoutConstraint.activate([
        timelineVC.view.topAnchor.constraint(equalTo: view.topAnchor),
        timelineVC.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
        timelineVC.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
        timelineVC.view.heightAnchor.constraint(equalTo: view.heightAnchor),
    ])
  }
  
}

