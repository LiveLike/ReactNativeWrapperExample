import EngagementSDK

class LiveLikeView: UIView {
  
  weak var separatedVideoViewController: SeparatedVideoViewController?
  private var sdk: EngagementSDK!
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupEngagementSDK()
  }
  
  @objc var widgetId = "" {
    didSet {
      //Here you can call any function and it will be called after the value get here
      //separatedVideoViewController.setDataWidget(data:data)

    }
  }
  
  @objc var widgetKind = "" {
    didSet {
      //Here you can call any function and it will be called after the value get here
      //separatedVideoViewController.setDataWidget(data:data)
    }
  }
  
  private func setupEngagementSDK() {
    sdk = EngagementSDK.init(config: EngagementSDKConfig(clientID: "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5"))
    EngagementSDK.logLevel = .debug
    
  }
  
  required init?(coder aDecoder: NSCoder) { fatalError("nope") }
  
  override func layoutSubviews() {
    super.layoutSubviews()
    
    if separatedVideoViewController == nil {
      embed()
    } else {
      separatedVideoViewController?.view.frame = bounds
    }
    separatedVideoViewController?.setWidgetData(widgetId: widgetId, widgetKind: widgetKind)
  }
  
  private func embed() {
    
    let vc = SeparatedVideoViewController(sdk : self.sdk)
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
