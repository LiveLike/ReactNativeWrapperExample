import React, {useEffect, useRef, useState} from 'react';
import {findNodeHandle, NativeModules, requireNativeComponent, UIManager} from 'react-native';

export const LiveLikeWidgetView = requireNativeComponent('LiveLikeWidgetView');

const clientId = '8PqSNDgIVHnXuJuGte1HdvOjOqhCFE1ZCR3qhqaS';
const programId = '28febd25-3e59-42ff-9549-4aa4198161a6';
const {LiveLikeModule} = NativeModules

const setProgram = (viewId) => {
    UIManager.dispatchViewManagerCommand(
        viewId,
        UIManager.LiveLikeWidgetView.Commands.setProgram.toString(),
        [viewId, programId,'text-poll']
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
            onReady={(event) => {
                const viewId = findNodeHandle(ref.current);
                UIManager.dispatchViewManagerCommand(
                viewId,
                UIManager.LiveLikeWidgetView.Commands.showWidget.toString(),
                [viewId])
            }}
            onWidgetShown={(event) => {
                console.log('DEBUG1:', 'widget shown')
            }}
            onWidgetHidden={(event) => {
                console.log('DEBUG2:', 'widget hidden')
            }}
        />
    )};
