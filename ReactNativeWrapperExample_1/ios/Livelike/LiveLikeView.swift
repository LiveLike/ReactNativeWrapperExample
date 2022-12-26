import EngagementSDK

class LiveLikeView: UIView {
  
  weak var separatedVideoViewController: SeparatedVideoViewController?
  
  override init(frame: CGRect) {
    super.init(frame: frame)
  
  }
    
  required init?(coder aDecoder: NSCoder) { fatalError("nope") }
  
  @objc var programId = "" {
      didSet {
        
        
      }
    }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    
    if separatedVideoViewController == nil {
      embed()
    } else {
      separatedVideoViewController?.view.frame = bounds
    }
    separatedVideoViewController?.setProgram(programId: programId)
  }
  
  private func embed() {
    
    let vc = SeparatedVideoViewController()
    let parentVC = parentViewController
    parentVC?.addChild(vc)
    addSubview(vc.view)
    vc.view.frame = bounds
    vc.didMove(toParent: parentVC)
    self.separatedVideoViewController = vc
  }
}

extension UIView {
  var parentViewController: UIViewController? {
    var parentResponder: UIResponder? = self
    while parentResponder != nil {
      parentResponder = parentResponder!.next
      if let viewController = parentResponder as? UIViewController {
        return viewController
      }
    }
    return nil
  }
  
  
}
