import AVKit
import EngagementSDK
import UIKit

class SeparatedVideoViewController: UIViewController {
  
  // MARK: EngagementSDK Properties
  private let widgetViewController = WidgetPopupViewController()
  
  init() {
    super.init(nibName: nil, bundle: nil)
  }
  
  
  // MARK: - UI Elements
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setContentSession() {
    widgetViewController.session = Livelike.DataProvider.shared.contentSession
  }
  
  // MARK: - UIViewController Life Cycle
  override func viewDidLoad() {
    super.viewDidLoad()
    setUpEngagementSDKLayout()
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

