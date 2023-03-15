import React, {useEffect, useRef} from 'react';
import { NativeModules, requireNativeComponent, NativeEventEmitter } from 'react-native';


const LiveLikeiOSView = requireNativeComponent("LiveLikeiOSView")

const Livelike = NativeModules.Livelike ? NativeModules.Livelike : new Proxy({}, {
    get() {
      throw new Error(LINKING_ERROR);
    }
  });

const eventEmitter = new NativeEventEmitter(Livelike);
const clientId = "vGgUtbZTQWW6C6ROKSqRAO9wdrZaGffXEzYIAxwQ"
const programId = "6ec5b5a8-286c-48be-89b1-83d84f800937"

export const RNLiveLikeiOSView = () => {

    eventEmitter.addListener('showNoWidgetView', event => {
      console.log('showNoWidgetView:', event);
    });
  
    eventEmitter.addListener('hideNoWidgetView', event => {
      console.log('hideNoWidgetView:', event);
    });
  
    const ref = useRef(null);
    useEffect(() => {
        (async () =>{
          await Livelike.initialize(clientId, '')
          await Livelike.startContentSession(programId)
          ref.current.setNativeProps({
            programId: programId,
          });
          //Test Code Temp Code for testing.. these props should be set on btn click
          setTimeout(() => {
            ref.current.setNativeProps({
              toggleWidget: 1,
            });
          }, 10000);

          
        })();
    }, [])
  return (
      
      <LiveLikeiOSView  style={{
        flex: 1
      }}
        ref={ref}></LiveLikeiOSView>
    )
};
