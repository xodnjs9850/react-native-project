import React, { Component, useState } from 'react';
import {Alert, NativeModules, Linking, Platform, BackHandler} from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import BluetoothStateManager from 'react-native-bluetooth-state-manager';
import RNDisableBatteryOptimizationsAndroid from '@brandonhenao/react-native-disable-battery-optimizations-android';
import DeviceInfo from 'react-native-device-info';
import {check, PERMISSIONS, RESULTS} from 'react-native-permissions';
import { startBatch } from 'mobx/dist/internal';
// import {
//     RESULT_SENSOR_VERIFY_KEY,
//     USE_SMART_ONEPASS_SYSTEM_KEY,
//     USE_SMART_PARKING_SYSTEM_KEY,
// } from '../Constants/Constants';
// import ThingsStore from '../AppState/ThingsStore';
// import {showErrorMessage, showWarningMessage} from './ErrorMessageService';
// import {HOST} from '../Constants/Config';


let BeaconServiceManager = NativeModules.BeaconService;
let SensorServiceManager = NativeModules.SensorService;
let usePurpose = '';
let resultSensorTest = null;
let userData = {};

export const isPlatformIOS = Platform.OS === 'ios';
export const isPlatformAndroid = Platform.OS === 'android';

// user 정보 가져오기
export const getUserData = async () => {
    await AsyncStorage.getItem('responseData', (err, result) => {
        const transData = JSON.parse(result);
        if(result != null) {
            userData = transData
        }
    })
};

export const checkBluetoothState = async () => {
    console.log('BeaconServiceManager: ', BeaconServiceManager);
    console.log('SensorServiceManager: ', SensorServiceManager);
    let result = await initialize();
    console.log('smartBeaconFunctionService: initialized..', result);
    return result;

    async function initialize() {

        // 블루투스 기능 확인
        return await BluetoothStateManager.getState()
            .then(async (bluetoothState) => {
                console.log('Bluetooth State is ', bluetoothState);
                switch (bluetoothState) {
                    case 'Unknown':
                    case 'Unsupported':
                        alert('이 스마트폰은 블루투스를 지원하지 않아\n공동현관 자동 문열림이나 주차위치 인식 기능을\n사용할 수 없습니다.');
                        return false;
                    case 'Unauthorized':
                        alert('공동현관 자동 문열림이나 주차위치 인식 기능을 사용하기 위해\n블루투스 사용 권한을 승인해 주시기 바랍니다.');
                        BluetoothStateManager.openSettings();
                        return false;
                    case 'Resetting':
                    case 'PoweredOff':
                        if (isPlatformAndroid) {
                            return await BluetoothStateManager.requestToEnable()
                                .then(async (result) => {
                                    return true;
                                });
                        }
                        break;
                    case 'PoweredOn':
                        return true;
                    default:
                        return false;
                }
            });
    }
};

/**
 * @param messageTitle
 */
export const checkPermisions = async (messageTitle: String) => {

    let permissionMessage = messageTitle + '을 위한 위치 조회 권한을 사용하실 수 없습니다.\n위치 조회 권한을 허용해야 합니다.';

    await Promise.all([
        check(PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION),
        check(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION),
        check(PERMISSIONS.ANDROID.ACCESS_BACKGROUND_LOCATION),
    ]).then(async ([accessCoarseLocation, accessFineLocation, accessBackgroundLocation]) => {
        console.log('StartBeaconService Check Permission: ', {
            accessCoarseLocation,
            accessFineLocation,
            accessBackgroundLocation,
        });
        if (accessCoarseLocation !== RESULTS.GRANTED || accessFineLocation !== RESULTS.GRANTED ||
            (accessBackgroundLocation === RESULTS.DENIED || accessBackgroundLocation === RESULTS.BLOCKED)) {
            Alert.alert(
                messageTitle + '의 사용을 위한 위치 조회 권한',
                permissionMessage,
                [{
                    text: '확인',
                    onPress: async () => {
                        Linking.openSettings();
                    },
                    style: 'cancel',
                },
                {
                    text: '취소',
                    onPress: async () => {
                        BackHandler.exitApp();
                    },
                    style: "cancel"
                }],
            );
        }
    });
    return true;
}

/**
 * @param messageTitle
 */
export const batteryPermitionCheck = async (messageTitle: String) => {
    
    // 배터리 최적화 설정 확인
    RNDisableBatteryOptimizationsAndroid.isBatteryOptimizationEnabled().then((isEnabled) => {
        if (isEnabled) {
            let batteryMessage = messageTitle + 
            ` 기능을 원활하게 사용하기 위해서는 휴대폰 설정의 [배터리 최적화/절약]에서 해당 앱 설정을 [배터리 사용량 최적화] 하지 않음으로 설정해주세요.`;
                Alert.alert(
                '배터리 최적화 제외 설정 안내',
                batteryMessage,
                [{
                    text: '확인',
                    onPress: async () => {
                        RNDisableBatteryOptimizationsAndroid.openBatteryModal();
                    }
                },
                {
                    text: '취소',
                    style: "cancel"
                }],
            );
        }
    });

    return false;

}

/**
 * @param onePass
 * @param useSmartParking
 */
export const startBeaconService = async (onePass: Boolean, useSmartParking: Boolean) => {

    usePurpose = 'none';

    if(onePass && useSmartParking) {
        usePurpose = 'both';
    } else if (useSmartParking) {
        usePurpose = 'parking';
    } else if (onePass) {
        usePurpose = 'onepass';
    }

    if (usePurpose === 'none') {
        return;
    }

    if(await checkBluetoothState()) {
        let messageTitle = '';
        switch (usePurpose) {
            case 'onepass':
                messageTitle = '공동현관 자동 문열림';
                break;
            case 'parking':
                messageTitle = '스마트 주차위치 인식';
                break;
            default:
                messageTitle = '스마트 주차 및 자동 문열림';
                break;
        }

        if (isPlatformAndroid) {

            // 배터리 사용량 최적화 기능(false)이 비활성화됐는지 확인하는 flag
            let checkBettery = await batteryPermitionCheck(messageTitle);
            
            // 위치 서비스 기능이 활성화(true) 됐는지 확인하는 flag
            let checkLocation = await checkPermisions(messageTitle);
            
            //console.log("checkLocation", checkLocation)
            //console.log("permision", checkBettery)

            if(!checkBettery && checkLocation) {
                await startService(usePurpose)
            }

        } 
        // else {
        //     // iOS Check Location Service
        //     await Promise.all([
        //         check(PERMISSIONS.IOS.LOCATION_ALWAYS),
        //         check(PERMISSIONS.IOS.LOCATION_WHEN_IN_USE),
        //     ]).then(async ([locationAlways, locationWhenInUse]) => {
        //         console.log('StartBeaconService Check Permission: ', {locationAlways, locationWhenInUse});
        //         if (locationAlways !== RESULTS.GRANTED || locationWhenInUse !== RESULTS.GRANTED) {
        //             Alert.alert(
        //                 messageTitle + '의 사용을 위한 위치 조회 권한',
        //                 permissionMessage,
        //                 [{
        //                     text: '확인',
        //                     onPress: async () => {
        //                     },
        //                     style: 'cancel',
        //                 }],
        //                 {cancelable: false},
        //             );
        //         } else {
        //             await startService(usePurpose);
        //         }
        //     });
        // }

    }

};

const startService = async (usePurpose) => {

    await getUserData();

    /* 사용자 정보
      비컨을 이용하여 문열림 기준을 판별하기 위해 보내주어야하는 데이터. */
    let reformData = {
        purpose: usePurpose,
        dong: userData.dong,
        ho: userData.ho,
        username: userData.name,
        isDriver: usePurpose !== 'onepass',
        parkingBeaconUUID: '201510058864-5654-4111710200-2108-01',
        onepass: [
            {
                minor: userData.minorList[0].minor,
                rssi: userData.minorList[0].rssi
            },
            {
                minor: userData.minorList[1].minor,
                rssi: userData.minorList[1].rssi
            }
        ]
,
    };
    console.log('userData.onepass : ', reformData.onepass);
    //console.log('startBeaconService: userData = ', reformData);

    try {
        // Native 에서 블루투스 시작하는 곳
        if (isPlatformAndroid) {
            let {result} = await BeaconServiceManager.StartBeaconService(reformData);
            console.log('startBeaconService StartBeaconService Result : ' + result);
            
            // 주차위치 인식 기능을 사용할때는 항상 Sensor Service 도 같이 시작해야 함.
            if (usePurpose !== 'onepass' && result === 'success') {
                let {result} = await SensorServiceManager.StartSensorService();
                console.log('startBeaconService StartSensorService Result: ' + result);
            }
            if (usePurpose === 'onepass') {
                await SensorServiceManager.CancelSensorService();
            }
        } else {
            BeaconServiceManager.StartBeaconService(reformData);
        }
    } catch (e) {
        console.log('startBeaconService : ', e);
    }
};