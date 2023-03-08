import EngagementSDK
import UIKit

class LandscapeWODView: UIView {
  
  weak var landscapeTimelineViewController: LandscapeTimelineViewController?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
  
  }
    
  required init?(coder aDecoder: NSCoder) { fatalError("nope") }
  
  @objc var programId = "" {
      didSet {
        landscapeTimelineViewController?.setContentSession()
      }
    }
  
  @objc var toggleWidget = false {
      didSet {
        landscapeTimelineViewController?.toggleWidgetVisibility()
      }
    }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    
    if landscapeTimelineViewController == nil {
      embed()
    } else {
      landscapeTimelineViewController?.view.frame = bounds
    }
    
  }
  
  private func embed() {
    
    let vc = LandscapeTimelineViewController()
    vc.view.backgroundColor = .gray
    let parentVC = parentViewController
    parentVC?.addChild(vc)
    addSubview(vc.view)
    vc.view.frame = bounds
    vc.didMove(toParent: parentVC)
    self.landscapeTimelineViewController = vc
  }
}
