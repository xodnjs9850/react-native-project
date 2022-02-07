import React, { Component, useEffect } from "react";
import { Text, View, ImageBackground, ActivityIndicator } from "react-native";
import {NavigationScreenProp, NavigationState} from 'react-navigation';
import {observer} from 'mobx-react';

import styles from '@utils/styles';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {
    isLoading: boolean,
}

export default observer(class introScreen extends Component<Props, State> {
    static navigationOptions = {headerShown: false};

    constructor(props: Props) {
        super(props);

        this.state = {
            isLoading: true,
        };
    }

    // _permitionCheck = async () => {

    //     let bluetoothFlag = false;
    //     let locationFlag = false;
    //     let batteryFlag = true;

    //     // 블루투스 기능 활성화 여부 체크
    //     bluetoothFlag = await checkBluetoothState();

    //     // 위치 권한 활성화 여부 체크
    //     locationFlag = await checkPermisions('공동현관 자동 문열림');
        
    //     // 배터리 최적화 기능 비활성화 여부 체크
    //     batteryFlag = await batteryPermitionCheck('[공동현관 및 주차위치 인식]');

    //     console.log("bluetooth : ", bluetoothFlag);
    //     console.log("location : ", locationFlag);
    //     console.log("battery : ", batteryFlag);

    //     if (locationFlag && bluetoothFlag && !batteryFlag) {
    //         this.props.navigation.navigate('loginScreen', {})
    //     }else {
    //         console.log("강제 종료");
    //         BackHandler.exitApp();
    //     }


    // }

    // _authService = async () => {

    //     // 기본 경고창 뛰움
    //     let messageTitle = "위치권한 사용 정보 알림";
    //     let permissionMessage = "이 앱은 로비[자동 문열림 기능]을 활성화하기 위해 [위치데이터]를 수집합니다.\n이 앱은 [앱이 닫혀 있을 때]에도 [항상 사용됨]을 알려드립니다.\n[백 그라운드]기능 사용은 앱이 자동으로 다시 켜졌을 때 비컨을 정상적으로 스캐닝하여 [자동 문열림기능]을 사용하기 위함입니다. 수집된 위치 정보는 로비 자동문열림에 사용됩니다.";
    //     Alert.alert(
    //         messageTitle,
    //         permissionMessage,
    //         [{
    //             text: '확인',
    //             onPress: async () => {
    //                 this._permitionCheck()
    //             },
    //             style: 'cancel',
    //         },
    //         {
    //             text: '취소',
    //             onPress: async () => {
    //                 BackHandler.exitApp();
    //             },
    //             style: "cancel"
    //         }],
    //     );
        
    // }


    render() {

        if(this.state.isLoading) {
            setTimeout(() => {
                this.props.navigation.navigate('loginScreen', {})
            }, 3000);
        }
                
        return (

            <View style={styles.root}>
                <ImageBackground
                    style={styles.rect}
                    imageStyle={styles.rect_imageStyle}
                    source={require("@assets/images/parking.jpg")}> 
                    <View style={styles.introScreen}>
                        <Text style={styles.logoText}>Smart Parking</Text>
                        <View style={styles.loadingAnimation}>
                            <ActivityIndicator
                                size = "large" color="#FFFFFF"/> 
                        </View>
                    </View>

                    

                </ImageBackground> 
            </View>
        );
    }
});

