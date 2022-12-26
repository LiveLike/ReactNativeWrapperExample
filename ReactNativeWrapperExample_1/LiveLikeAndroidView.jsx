import React, {useEffect, useRef, useState} from 'react';
import {findNodeHandle, NativeModules, requireNativeComponent, UIManager} from 'react-native';
import { PixelRatio } from "react-native";

export const LiveLikeWidgetView = requireNativeComponent('LiveLikeWidgetView');

const clientId = "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5"
const programId = "71add52f-dd99-42ac-8e96-743aaad41c3b"
const {LiveLikeModule} = NativeModules

const setProgram = (viewId) => {
    UIManager.dispatchViewManagerCommand(
        viewId,
        UIManager.LiveLikeWidgetView.Commands.setProgram.toString(),
        [viewId, programId]
    );
}

export const LiveLikeAndroidView = () => {

    const ref = useRef(null);
    useEffect(() => {
        (async () =>{
            await LiveLikeModule.initializeSDK(clientId)
            const viewId = findNodeHandle(ref.current);
            setProgram(viewId);
        })();
       
    }, [])

    return (
        <LiveLikeWidgetView
            style={{
                // converts dpi to px, provide desired height
                height: PixelRatio.getPixelSizeForLayoutSize(200),
                // converts dpi to px, provide desired width
                width: PixelRatio.getPixelSizeForLayoutSize(145)
            }}
            ref={ref}
            onWidgetShown={(event) => {
                console.log('DEBUG1:', 'widget shown')
            }}
            onWidgetHidden={(event) => {
                console.log('DEBUG2:', 'widget hidden')
            }}
        />
    )};
