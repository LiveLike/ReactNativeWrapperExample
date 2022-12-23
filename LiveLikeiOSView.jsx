import React, {useEffect, useRef} from 'react';
import {View, NativeModules, UIManager,NativeEventEmitter} from 'react-native';
import { PixelRatio } from "react-native";

const clientId = "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5"
const programId = "71add52f-dd99-42ac-8e96-743aaad41c3b"
const { Livelike } = NativeModules
const eventEmitter = new NativeEventEmitter(Livelike);

const setProgram = (viewId) => {
    UIManager.dispatchViewManagerCommand(
        viewId,
        UIManager.LiveLikeWidgetView.Commands.setProgram.toString(),
        [viewId, programId]
    );
}

const onWidgetReceived = (event) => {
    console.log("** WidgetReceived",event);  
  };
 
const subscription = eventEmitter.addListener('onWidgetReceived', onWidgetReceived);
  
    
export const LiveLikeiOSView = () => {

    const ref = useRef(null);
    useEffect(() => {
        (async () =>{
          await Livelike.initialize(clientId, '')
          await Livelike.startContentSession('3e5ccef5-7d06-4a1c-a51f-032b7692da93')
          await Livelike.joinProducerChatRoom('c855bea2-06c6-4e42-88a1-d4db502432a2')
          await Livelike.joinChatRoom('8672fa25-21c5-416b-8583-b5b81c4ec377','')
        })();
    }, [])

    return (
        <View
            style={{
                // converts dpi to px, provide desired height
                height: PixelRatio.getPixelSizeForLayoutSize(200),
                // converts dpi to px, provide desired width
                width: PixelRatio.getPixelSizeForLayoutSize(145)
            }}
            ref={ref}
        />
    )};
