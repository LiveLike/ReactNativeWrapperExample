import AVKit
import EngagementSDK
import UIKit

class SeparatedVideoViewController: UIViewController {
  // MARK: EngagementSDK Properties
  
  private var contentSession: ContentSession? = nil
  private let widgetViewController = WidgetPopupViewController()
  
  init() {
    super.init(nibName: nil, bundle: nil)
  }
  
  private var isLandscapeMode: Bool {
    if UIScreen.main.bounds.size.width > UIScreen.main.bounds.size.height {
      return true
    }
    return false
  }
  
  private var landscapeWidgetViewLeading: NSLayoutConstraint?
  var timeObserver: Any?
  lazy var dateFormatter: DateFormatter = {
    DateFormatter.currentTimeZoneTime
  }()
  
  private var portraitConstraints: [NSLayoutConstraint] = Array()
  private var landscapeConstraints: [NSLayoutConstraint] = Array()
  
  // MARK: - UI Elements
  
  private var widgetView: UIView = UIView()
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setProgram(programId: String) {
    let config = SessionConfiguration(programID: programId)
    contentSession = Livelike.DataProvider.shared.engagementSDK.contentSession(config: config)
    widgetViewController.session = contentSession
  }
  
  // MARK: - UIViewController Life Cycle
  override func viewDidLoad() {
    super.viewDidLoad()
    setUpEngagementSDKLayout()
  }
  
  override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
  }
  
  override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
    super.viewWillTransition(to: size, with: coordinator)
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  

  deinit {
    NotificationCenter.default.removeObserver(self)
  }
  
  // MARK: - UIButton Actions
  
  @objc func backButtonAction(sender: UIButton) {
    dismiss(animated: true, completion: nil)
  }
}

extension SeparatedVideoViewController {
  private func setUpEngagementSDKLayout() {
    // Add `widgetViewController` as child view controller
    addChild(widgetViewController)
    widgetViewController.didMove(toParent: self)

    // Apply constraints to the `widgetViewController.view`
    widgetViewController.view.translatesAutoresizingMaskIntoConstraints = false
    view.addSubview(widgetViewController.view)
    NSLayoutConstraint.activate([
      widgetViewController.view.topAnchor.constraint(equalTo: view.topAnchor),
      widgetViewController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
      widgetViewController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
      widgetViewController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor)
    ])
  }
  
}

