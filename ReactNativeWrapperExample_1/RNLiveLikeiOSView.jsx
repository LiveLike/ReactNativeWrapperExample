import React, {useEffect, useRef} from 'react';
import {NativeModules, requireNativeComponent} from 'react-native';
const LiveLikeiOSView = requireNativeComponent("LiveLikeiOSView")
const Livelike = NativeModules.Livelike ? NativeModules.Livelike : new Proxy({}, {
    get() {
      throw new Error(LINKING_ERROR);
    }
  });

const clientId = "mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5"
const programId = "71add52f-dd99-42ac-8e96-743aaad41c3b"

export const RNLiveLikeiOSView = () => {
    const ref = useRef(null);
    useEffect(() => {
        (async () =>{
          await Livelike.initialize(clientId, '')
        })();
    }, [])

    return (
        <LiveLikeiOSView ref={ref}></LiveLikeiOSView>
    )
};
