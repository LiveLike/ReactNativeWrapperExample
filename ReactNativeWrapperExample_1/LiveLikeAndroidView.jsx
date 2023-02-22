import React, {useEffect, useRef, useState} from 'react';
import {findNodeHandle, NativeModules, requireNativeComponent, UIManager} from 'react-native';

export const LiveLikeWidgetView = requireNativeComponent('LiveLikeWidgetView');

const clientId = 'itS3XnEs8VaJbPoioJGMvGl7DnlYGYY869k9z213';
const programId = 'f68581a6-a28a-41ba-bcf8-7ebe18c4bb55';
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
        (async () => {
            await LiveLikeModule.initializeSDK(clientId)
            const viewId = findNodeHandle(ref.current);
            setProgram(viewId);
        })();
       
    }, [])
    
    return (
        <LiveLikeWidgetView
            style={{
                flex:1
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
