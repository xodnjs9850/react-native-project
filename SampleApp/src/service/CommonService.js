import React from 'react';
//import RNSecureKeyStore, {ACCESSIBLE} from 'react-native-secure-key-store';
import {
    ToastAndroid,
    } from "react-native";


/**
 * showToast
 * @param title
 */
 export const showToast = (title: string) => {
    ToastAndroid.show(title, ToastAndroid.SHORT);

};


// /**
//  * setStorageValue
//  * @param key
//  * @param value
//  * @returns {Promise<void>}
//  */
//  export const setStorageValue = async (key: string, value: string) => {
//     await RNSecureKeyStore.set(key, value, {accessible: ACCESSIBLE.ALWAYS_THIS_DEVICE_ONLY})
//         .catch((err) => {
//             console.log(`setStorageValue(${key}) Error = ${err}`);
//         });
// //  await AsyncStorage.setItem(key, value);
// };




    
    
