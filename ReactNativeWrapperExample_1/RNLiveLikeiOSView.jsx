import React, {useEffect, useRef} from 'react';
import {NativeModules, requireNativeComponent} from 'react-native';
const LiveLikeiOSView = requireNativeComponent("LiveLikeiOSView")
const Livelike = NativeModules.Livelike ? NativeModules.Livelike : new Proxy({}, {
    get() {
      throw new Error(LINKING_ERROR);
    }
  });

const clientId = "vGgUtbZTQWW6C6ROKSqRAO9wdrZaGffXEzYIAxwQ"
const programId = "285e4d20-b60c-415d-9624-394646b4471a"

export const RNLiveLikeiOSView = () => {
    const ref = useRef(null);
    useEffect(() => {
        (async () =>{
          await Livelike.initialize(clientId, '')
          await Livelike.startContentSession(programId)
          ref.current.setNativeProps({
            programId: programId,
          });
        })();
    }, [])
  return (
      
      <LiveLikeiOSView  style={{
        flex: 1
      }}
        ref={ref}></LiveLikeiOSView>
    )
};
