import { StatusBar } from 'expo-status-bar';
import React, {useEffect, useRef, useState} from 'react';
import { StyleSheet, Platform, View } from 'react-native';
import { UIManager, findNodeHandle, requireNativeComponent, NativeModules } from 'react-native';
import { LiveLikeAndroidView } from './LiveLikeAndroidView';
const LiveLikeiOSView = requireNativeComponent("LiveLikeiOSView")

const Livelike = NativeModules.Livelike ? NativeModules.Livelike : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});

export default function App() {
  const isIOS = Platform.OS == 'ios';
  const ref = useRef(null);
    useEffect(() => {
        (async () =>{
          await Livelike.initialize("mOBYul18quffrBDuq2IACKtVuLbUzXIPye5S3bq5", '')
        })();
    }, [])
  return (
    <View style={styles.container}>
     {isIOS ? (
        <LiveLikeiOSView style={styles.wrapper} programId="71add52f-dd99-42ac-8e96-743aaad41c3b" ref={ref}></LiveLikeiOSView>
      ) : (
        <LiveLikeAndroidView style={styles.wrapper}></LiveLikeAndroidView>
      )}
      <StatusBar style="auto" />
    </View>
  );
}


const styles = StyleSheet.create({
  container: {
    flex: 1, alignItems: "stretch"
  },
  wrapper: {
    flex: 1, alignItems: "center", justifyContent: "center"
  },
  border: {
    borderColor: "#eee", borderBottomWidth: 1
  },
  button: {
    fontSize: 50, color: "orange"
  }
});
