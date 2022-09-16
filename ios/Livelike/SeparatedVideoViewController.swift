//
//  SeparatedVideoViewController.swift
//  LiveLikeDemoApp
//
//  Created by Mike M on 7/24/19.
//

import AVKit
import EngagementSDK
import UIKit

class SeparatedVideoViewController: UIViewController {
  // MARK: EngagementSDK Properties
  
  private let sdk: EngagementSDK
  private var widgetId:String = ""
  private var widgetKind:String = ""
  
  public func setWidgetData(widgetId:String, widgetKind:String) {
    self.widgetId = widgetId
    self.widgetKind = widgetKind
    getWidgetAndShow()
  }

  init(sdk: EngagementSDK) {
    self.sdk = sdk
    super.init(nibName: nil, bundle: nil)
  }
  
  public func setDataWidget(JSON:NSString) {
  
  }
  
  // MARK: - UI Elements
  private let widgetView: UIView = {
      let widgetView = UIView()
      widgetView.translatesAutoresizingMaskIntoConstraints = false
      widgetView.isHidden = true
      return widgetView
  }()
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  // MARK: - UI Setup
  
  private func setUpViews() {
    view.addSubview(widgetView)
  }
  
 
  

  private var widgetStateController = DefaultWidgetStateController(
      closeButtonAction: {},
      widgetFinishedCompletion: { _ in }
  )
  
  // MARK: - UIViewController Life Cycle
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    setUpEngagementSDKLayout()
    setUpViews()
    setUpLayout()
  }
  
  private func setUpLayout() {
    widgetView.topAnchor.constraint(equalTo: view.safeTopAnchor, constant: 0.0).isActive = true
    widgetView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: 0.0).isActive = true
    widgetView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 0.0).isActive = true

  }

  private func getWidgetAndShow() {
    sdk.getWidget(id: self.widgetId, kind: WidgetKind(stringValue: widgetKind)!) {
          result in
          switch result {
            case .success(let widget):
              // present `Widget` to your user
            self.presentWidget(widget: widget)
            case .failure(let error):
              // Something went wrong
              print(error)
          }
      }
    
  }
  
  private func presentWidget(widget: Widget) {
      DispatchQueue.main.async { [weak self] in
          guard let self = self else { return }

          self.widgetView.isHidden = false
          self.addChild(widget)

          widget.view.translatesAutoresizingMaskIntoConstraints = false
          self.widgetView.addSubview(widget.view)
          NSLayoutConstraint.activate([
              
              widget.view.topAnchor.constraint(equalTo: self.widgetView.topAnchor),
              widget.view.leadingAnchor.constraint(equalTo: self.widgetView.leadingAnchor),
              widget.view.trailingAnchor.constraint(equalTo: self.widgetView.trailingAnchor),
              widget.view.bottomAnchor.constraint(equalTo: self.widgetView.bottomAnchor, constant: 0)
          ])

          widget.didMove(toParent: self)
          widget.moveToNextState()
      }
  }
}

// MARK: - Engagement SDK Functionality

extension SeparatedVideoViewController {
  private func setUpEngagementSDKLayout() {
    // Add widgetViewController as child view controller
    widgetView.translatesAutoresizingMaskIntoConstraints = false
  }
}

extension UIView {
    var safeTopAnchor: NSLayoutYAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.topAnchor
      }
      return topAnchor
    }
    
    var safeLeftAnchor: NSLayoutXAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.leftAnchor
      }
      return leftAnchor
    }
    
    var safeRightAnchor: NSLayoutXAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.rightAnchor
      }
      return rightAnchor
    }
    
    var safeBottomAnchor: NSLayoutYAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.bottomAnchor
      }
      return bottomAnchor
    }
    
    var safeTrailingAnchor: NSLayoutXAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.trailingAnchor
      }
      return trailingAnchor
    }
    
    var safeLeadingAnchor: NSLayoutXAxisAnchor {
      if #available(iOS 11.0, *) {
        return self.safeAreaLayoutGuide.leadingAnchor
      }
      return leadingAnchor
    }
}

