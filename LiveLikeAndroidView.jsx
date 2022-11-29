import React, {useEffect, useRef, useState} from 'react';
import {findNodeHandle, NativeModules, requireNativeComponent, UIManager} from 'react-native';

export const LiveLikeWidgetView = requireNativeComponent('LiveLikeWidgetView');

const clientId = "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5"
const programId = "71add52f-dd99-42ac-8e96-743aaad41c3b"
const {LiveLikeModule} = NativeModules

const updateNickName = (viewId, nickName) => {
    UIManager.dispatchViewManagerCommand(
        viewId,
        UIManager.LiveLikeChatWidgetView.Commands.updateNickName.toString(),
        [viewId, nickName]
    );
}

const TIME_DELAY = 13000

export const LiveLikeAndroidView = () => {

    const ref = useRef(null);
    useEffect(() => {
        LiveLikeModule.initializeSDK(clientId)
    }, [])

    return (
        <View style={{
            marginTop: 12,
            height: 500,
            width: '100%',
            position: 'absolute',
            left: 0,
            top: 0
        }}>
            <LiveLikeWidgetView
                programId={programId}
                showAskWidget={show}
                style={{flex: 1}}
                onWidgetShown={(event) => {
                    console.log('DEBUG1:', 'widget shown')
                }}
                onWidgetHidden={(event) => {
                    console.log('DEBUG2:', 'widget hidden')
                }}
            />
        </View>
    )};
