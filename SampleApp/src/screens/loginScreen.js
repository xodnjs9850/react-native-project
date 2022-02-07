import React, { Component } from 'react';
import {observer} from 'mobx-react';
import {NavigationScreenProp, NavigationState } from 'react-navigation';
import axios from 'axios';
import {sha256} from 'react-native-sha256';
import base64 from 'base-64';
import Icon from 'react-native-vector-icons/AntDesign';
import CheckBox from '@react-native-community/checkbox';
import AsyncStorage from '@react-native-community/async-storage';
import {
    View,
    StatusBar,
    ImageBackground,
    Text,
    TextInput,
    TouchableOpacity, 
    KeyboardAvoidingView,
    Keyboard,
    BackHandler,
    Alert,
    } from "react-native";

import { checkBluetoothState, checkPermisions, batteryPermitionCheck, startBeaconService } from '@service/SmartBeaconFunctionService';
import { showToast } from '@service/CommonService';
import styles from '@utils/styles';

const HARDWARE_BACK_PRESS_EVENT = 'hardwareBackPress';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {
    userId: string,
    password: string,
    settingId: boolean,
    settingPw: boolean,
    autoLogin: boolean,
    useSmartParking: boolean,
    sensorTestFlag: boolean,
    onePass: Boolean,
}


export default observer(class SensorStart extends Component<Props, State> {
    //static navigationOptions = {headerShown: false};

    exitApp: boolean = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            userId: '',
            password: '',
            settingId: false,
            settingPw: false,
            autoLogin: false,
            useSmartParking: false,
            sensorTestFlag: false,
            onePass: true,
        };
    }

    async componentDidMount(): void {

        BackHandler.addEventListener(HARDWARE_BACK_PRESS_EVENT, this._handleBackButton);

        // 로그인 정보 있을 시 불러오기
        AsyncStorage.getItem('loginData', (err, result) => {
            if(result != null) {
                this.setState(JSON.parse(result))
            }
            else {
                this._removeLoginData()
            }
        })

        // sensor test 통과 여부
        AsyncStorage.getItem('sensorTestResult', (err, result) => {
            this.setState({sensorTestFlag: result})
        })

        // 기본 경고창 뛰움
        let messageTitle = "위치권한 사용 정보 알림";
        let permissionMessage = "이 앱은 로비[자동 문열림 기능]을 활성화하기 위해 [위치데이터]를 수집합니다.\n이 앱은 [앱이 닫혀 있을 때]에도 [항상 사용됨]을 알려드립니다.\n[백 그라운드]기능 사용은 앱이 자동으로 다시 켜졌을 때 비컨을 정상적으로 스캐닝하여 [자동 문열림기능]을 사용하기 위함입니다. 수집된 위치 정보는 로비 자동문열림에 사용됩니다.";
        Alert.alert(
            messageTitle,
            permissionMessage,
            [{
                text: '확인',
                onPress: async () => {
                    this._permitionCheck();
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

    componentWillUnmount() {
        this.exitApp = false;
        BackHandler.removeEventListener(HARDWARE_BACK_PRESS_EVENT, this._handleBackButton);
    }

    _permitionCheck = async () => {

        // 블루투스 기능 활성화 여부 체크
        await checkBluetoothState();

        // 위치 권한 활성화 여부 체크
        await checkPermisions('공동현관 자동 문열림');
        
        // 배터리 최적화 기능 비활성화 여부 체크
        await batteryPermitionCheck('[공동현관 및 주차위치 인식]');

    }

    handleSubmitPress = () => {
        if (this.state.settingId && this.state.settingPw) {

            sha256(this.state.password).then((hash) => {
                const shaPass = hash;
                const basePass = base64.encode(shaPass);

                let tempStr = this.state.userId
                this.setState({
                    userId: tempStr.replace(/(\s*)/g, "")
                })
    
                axios({
                    "method": "POST",
                    "url": "http://211.240.121.123:8080/pms-server-web/app/login",
                    "headers": {
                        "content-type": "nmultipart/form-data"
                    }, "params": {
                        "id": this.state.userId,
                        "pass": basePass,
                        "site":"lakeedutown"
                    }
                    })
                    .then((response) => {
                        if(response.data.returnCode === -1) {
                            alert("아이디 혹은 패스워드를 확인해주세요.\n");
                        } else {
                            //this.props.navigation.navigate('SensorStartScreen')
                            //console.log(response.data)
                            responseData = {   
                                cel: response.data.result.cel,
                                dong: response.data.result.dong,
                                ho: response.data.result.ho,
                                name: response.data.result.name,
                                minorList: response.data.result.minorList,
                            }

                            AsyncStorage.setItem('responseData', JSON.stringify(responseData));

                            if(this.state.autoLogin) {
                                this._saveLoginData();
                            } else {
                                this._removeLoginData();
                            }
                            
                            if(this.state.useSmartParking) {

                                if(this.state.sensorTestFlag) {
                                    //this.props.navigation.navigate('homeScreen', {})
                                    this._startBeaconService();
                                } 
                                else {
                                    this.props.navigation.navigate('SensorStartScreen', {})
                                }

                            } else{
                                //this.props.navigation.navigate('homeScreen', {})
                                this._startBeaconService();
                            }

                        }
                    })
                    .catch((error) => {
                        console.log(error)
                    })
            });
        } 
        else {
            alert('아이디 혹은 패스워드를 확인해주세요.');
            this.setState({
                userId: '',
                password: '',
                settingPw: false,
                settingId: false,
            })
            return;
        }

    };

    _handleBackButton = () => {

        if (this.exitApp === undefined || !this.exitApp) {
            showToast('한번 더 누르시면 어플을 종료합니다.');
            this.exitApp = true;
            this.timeout = setTimeout(
                () => {
                    this.exitApp = false;
                },
                2000,    // 2초
            );
        } else {
            clearTimeout(this.timeout);
            BackHandler.exitApp();  // 앱 종료
        }
        return true;
    };

    _saveLoginData = () => {
        AsyncStorage.setItem('loginData', JSON.stringify(
            this.state
        ));
    }

    _removeLoginData = () => {
        this.setState({
            userId: '',
            password: '',
            settingId: false,
            settingPw: false,
            autoLogin: false,
        })

        AsyncStorage.setItem('loginData', JSON.stringify(
            this.state
        ));
    }

    _startBeaconService = async () => {

        await startBeaconService(this.state.onePass, this.state.useSmartParking);
        BackHandler.exitApp();

    };

    render() {
        return(
            <View style={styles.root}>
                <StatusBar barStyle="light-content" backgroundColor="rgba(0,0,0,1)" />
                <KeyboardAvoidingView style={styles.background}>
                    <ImageBackground
                    style={styles.rect}
                    imageStyle={styles.rect_imageStyle}
                    source={require("@assets/images/parking.jpg")}
                    >
                    <View style={styles.logoColumn}>
                        <View style={styles.logo}>
                        <View style={styles.endWrapperFiller}></View>
                        <View style={styles.text3Column}>
                            <Text style={styles.text3}>Smart Parking</Text>
                            <View style={styles.rect7}></View>
                        </View>
                        </View>
                        <View style={styles.form}>
                            <View style={styles.usernameColumn}>
                                <View style={styles.username}>
                                    <Icon name="user" style={styles.icon22} />
                                    <TextInput
                                        style={styles.usernameInput}
                                        value={this.state.userId}
                                        onChangeText={(userIdText) =>
                                            this.setState({
                                                userId: userIdText,
                                                settingId: true,
                                            })}
                                        placeholder="Enter User ID"
                                        placeholderTextColor="rgba(255,255,255,1)"
                                        autoCapitalize="none"
                                        keyboardType="default"
                                        returnKeyType="next"
                                        underlineColorAndroid="#f000"
                                        blurOnSubmit={false}
                                    />
                                </View>
                                <View style={styles.password}>
                                    <Icon
                                        name="lock"
                                        style={styles.icon2}
                                    ></Icon>
                                    <TextInput
                                        style={styles.passwordInput}
                                        value={this.state.password}
                                        onChangeText={(userPassword) =>
                                            this.setState({
                                                password: userPassword,
                                                settingPw: true,
                                            })}
                                        placeholder="Enter Password"
                                        placeholderTextColor="rgba(255,255,255,1)"
                                        keyboardType="default"
                                        
                                        onSubmitEditing={Keyboard.dismiss}
                                        blurOnSubmit={false}
                                        secureTextEntry={true}
                                        underlineColorAndroid="#f000"
                                        returnKeyType="next"
                                    />
                                </View>
                            </View>
                            <View style={styles.usernameColumnFiller}>
                                <TouchableOpacity
                                    style={styles.button}
                                    activeOpacity={0.5}
                                    onPress={this.handleSubmitPress}>
                                    <Text style={styles.text2}>LOGIN</Text>
                                </TouchableOpacity>

                                <View style={styles.checkBoxContainer}>
                                    <CheckBox
                                    disabled={false}
                                    value={this.state.autoLogin}
                                    tintColors={{false: '#f0edf2'}}
                                    onValueChange={(loginValue) =>
                                        this.setState({autoLogin: loginValue})
                                    }/>
                                    <Text style={styles.checkBoxText}>계정 기억하기</Text>
                                    <CheckBox
                                    disabled={false}
                                    value={this.state.useSmartParking}
                                    tintColors={{false: '#f0edf2'}}
                                    onValueChange={(parkingValue) =>
                                        this.setState({useSmartParking: parkingValue})
                                    }/>
                                    <Text style={styles.checkBoxText}>주차 서비스 사용하기</Text>
                                </View>

                            </View>
                        </View>
                    </View>
                    </ImageBackground>
                </KeyboardAvoidingView>
            </View>
        );
    }
});

