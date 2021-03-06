import React, { Component } from 'react';
import {observer} from 'mobx-react';
import {NavigationScreenProp, NavigationState} from 'react-navigation';
import AsyncStorage from '@react-native-community/async-storage';

import { startBeaconService } from '@service/SmartBeaconFunctionService';
import {
    View, 
    Text, 
    TouchableOpacity, 
    Image, 
    Animated, 
    Easing,
    NativeModules,
    ImageBackground,
    BackHandler,
    } from 'react-native';
import styles from '@utils/styles';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {
    stopAnimation: boolean,
    rotateAnimation: any,
    cancelState: Boolean,

    isStartedSensor: boolean,
    successCheckSensorTest: boolean,
    checkStage: string,
    checkPrompt: string,

    progressTime: number,
}

let SensorVerify = NativeModules.SensorVerify;

const SENSOR_TEST_INTERVAL = 5000;
const ANIMATION_INTERVAL = 1500;
const RESULT_SENSOR_VERIFY_KEY = 'resultSensorVerify';

export default observer(class sensorTestScreen extends Component<Props, State> {
    static navigationOptions = {headerShown: false};

    sensorTestInterval: any = null;
    
    constructor(props: Props) {
        super(props);

        this.rotateAnimation = new Animated.Value(0);

        this.state = {
            stopAnimation: false,
            isStartedSensor: false,
            successCheckSensorTest: false,
            checkStage: '',
            checkPrompt: '주차위치 인식을 위한\n스마트폰의 센서를 테스트합니다.',
            progressTime: -1,
            cancelState: false,
        };

    }

    async componentDidMount() {
        console.log('SensorVerify: ', SensorVerify);

        try {
            await SensorVerify.setPostDelayTime(SENSOR_TEST_INTERVAL);
            let {result} = await SensorVerify.StartSensorVerify();
            console.log('Sensor Verify Service Started: ', result);
            if (result === true) {
                this.setState({
                    isStartedSensor: true,
                }, () => {
                    this._checkSensorVerifyRight();
                });
            } else {
                console.log('센서를 등록할 수 없습니다.\n관리자에게 문의하세요.');
            }
        } catch (e) {
            console.log('Register sensor error: ', e);
            console.log('센서 설정에 오류가 있습니다.\n관리자에게 문의하세요.');
        }
    }

    async componentWillUnmount() {

        this.clearSensorTestInterval(true);

        if (this.state.isStartedSensor) {
            try {
                let {result} = await SensorVerify.EndSensorVerify();
                console.log('End sensor verify result: ', result);
            } catch (e) {
                console.log('End sensor verify error: ', e);
            }
        }
    }

    _startRotateAnimation = (angle, duration) => {
        
        Animated.timing(this.rotateAnimation, {
            toValue: angle,
            duration: duration,
            easing: Easing.linear,
            useNativeDriver: false
        }).start();

        this.setState({
            progressTime: 5,
        }, () => {
            this.sensorTestInterval = setInterval(() => {
                if (this.state.progressTime <= 0) {
                    this.clearSensorTestInterval(true);
                } else {
                    this.setState({
                        progressTime: this.state.progressTime - 1,
                    });
                }
            }, 1000);
        });
        
    };

    _stopRotateAnimation = () => {
        this.rotateAnimation.setValue(0);
        this.rotateAnimation.stopAnimation();
    };

    clearSensorTestInterval = (isSuccess) => {
        if (isSuccess) {
            clearInterval(this.sensorTestInterval);
            this.sensorTestInterval = null;
            this.setState({
                progressTime: -1,
            });
        }
        if (!isSuccess) {
            this._stopRotateAnimation();

            if(this.state.cancelState) {
                return
            }
            this.setState({
                checkStage: 'fail',
                checkPrompt: '센서 테스트에 실패하였습니다.\n처음부터 다시 테스트 부탁드립니다.',
            });
        }
    };

    _checkSensorVerifyRight = () => {
        if (!this.state.isStartedSensor) {
            return;
        }
        this.setState({
            stopAnimation: false,
            checkStage: 'right',
            checkPrompt: '스마트폰을 오른쪽으로\n기울여 멈춰 주세요.',
        }, async () => {
            this._startRotateAnimation(0.25, ANIMATION_INTERVAL);
            try {
                let {state, result} = await SensorVerify.RightSensorVerify();
                console.log('Sensor test RIGHT: ', state, result);

                if (state === 'Right' && result === true) {
                    this._stopRotateAnimation();
                    this.clearSensorTestInterval(true);
                    this._checkSensorVerifyLeft();
                } else {
                    this.clearSensorTestInterval(false);
                }
            } catch (e) {
                console.log('Sensor test RIGHT error: ', e);
                this.clearSensorTestInterval(false);
            }
        });
    };

    _checkSensorVerifyLeft = () => {
        this.setState({
            checkStage: 'left',
            checkPrompt: '스마트폰을 왼쪽으로\n기울여 멈춰 주세요.',
        }, async () => {
            this._startRotateAnimation(-0.25, ANIMATION_INTERVAL);
            try {
                let {state, result} = await SensorVerify.LeftSensorVerify();
                console.log('Sensor test LEFT: ', state, result);
                if (state === 'Left' && result === true) {
                    this.clearSensorTestInterval(true);
                    this._checkSensorVerifyStay();
                } else {
                    this.clearSensorTestInterval(false);
                }
            } catch (e) {
                console.log('Sensor test LEFT error: ', e);
                this.clearSensorTestInterval(false);
            }
        });
    };

    _checkSensorVerifyStay = () => {
        this.setState({
            checkStage: 'stay',
            checkPrompt: '스마트폰을 똑바로\n세워 주세요.',
        }, async () => {
            this._startRotateAnimation(0.0, ANIMATION_INTERVAL);
            try {
                let {state, result} = await SensorVerify.StaySensorVerify();
                console.log('Sensor test STAY: ', state, result);
                if (state === 'Stay' && result === true) {
                    this.setState({
                        checkPrompt: '스마트폰의 센서 테스트를\n정상적으로 종료하였습니다.',
                        successCheckSensorTest: true,
                    }, async () => {
                        this.clearSensorTestInterval(true);
                        await this.successSensorTest();
                    });
                } else {
                    this.clearSensorTestInterval(false);
                }
            } catch (e) {
                console.log('Sensor test STAY error: ', e);
                this.clearSensorTestInterval(false);
            }
        });
    };

    successSensorTest = async () => {
        AsyncStorage.setItem('sensorTestResult', JSON.stringify(
            this.state.successCheckSensorTest
        ));
    };

    onPress = () => {

        this.setState({cancelState: true});

        if(this.state.successCheckSensorTest) {
            //this.props.navigation.navigate('homeScreen') 
            startBeaconService(true, true);
            BackHandler.exitApp();
        } else {
            //this.props.navigation.replace('SensorStartScreen', {})
            //this.props.navigation.pop();
            this.props.navigation.navigate('SensorStartScreen', {})
        }
    }

    render() {
        const interpolateRotation = this.rotateAnimation.interpolate({
            inputRange: [0, 1],
            outputRange: ['0deg', '360deg'],
        });
        const rotatedStyle = {
            transform: [{
                rotate: interpolateRotation,
            }],
        };

        return (
            <View style={styles.root}>
                {/* Body */}
                <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                    <Animated.View style={rotatedStyle}>
                        <ImageBackground style={{width: 154, height: 240}}
                                         source={require('@assets/images/phone_stay.png')}
                                         resizeMode={'contain'}>
                            {this.state.successCheckSensorTest &&
                            <View style={styles.successCheckBox}>
                                <Image style={styles.imageSize42}
                                       source={require('@assets/images/phone_complit.png')}
                                       resizeMode={'contain'}/>
                            </View>
                            }
                        </ImageBackground>
                    </Animated.View>
                    
                    {this.state.checkStage === '' ?
                        <View>
                            <Text style={[styles.sensorPrompt, {fontSize: 14, lineHeight: 24}]}>
                                {'주차위치 인식을 위한 스마트폰의 센서를 테스트합니다.'}
                            </Text>
                        </View>
                        :
                        <Text style={[styles.sensorPrompt, {fontSize: 16, lineHeight: 24}]}>
                            {this.state.checkPrompt}
                        </Text>
                    }

                    {!this.state.successCheckSensorTest &&
                    <Text style ={[styles.sensorPrompt, {fontSize: 14, lineHeight: 24}]}>
                        {this.state.checkStage !== 'fail' ?
                            '스마트폰을 눈 앞에 90도 각도로 세워주세요.'
                            :
                            '센서테스트가 실패할 경우 화면 자동회전 기능을\n OFF 한 후 다시 시도해주세요.'}
                    </Text>
                    }
                    {this.state.progressTime > -1 &&
                    <View style={styles.progressTimeView}>
                        <Text style={styles.progressTimeText}>
                            {this.state.progressTime}
                        </Text>
                    </View>
                    }
                </View>
                {/* 센서 테스트 취소 */}
                <View style={[styles.saveBox, {marginBottom: 15}]}>
                    <TouchableOpacity
                        style={[styles.button3]}
                        onPress={this.onPress}>
                        <View
                            style={ {backgroundColor: this.state.successCheckSensorTest ? '#0f2027' : '#0f2027'}}/>
                        <Text style={[styles.saveTextWhite]}>
                            {this.state.successCheckSensorTest ? '완료' : '취소'}
                        </Text>
                    </TouchableOpacity>
                    {(this.state.checkStage === 'fail' || this.state.checkStage === '') &&
                    <TouchableOpacity
                        activeOpacity={0.8}
                        style={[styles.button3]}
                        onPress={async () => {
                            this._checkSensorVerifyRight();
                        }}>
                        <Text style={[styles.saveTextWhite]}>
                            {this.state.checkStage === '' ? '시작' : '재시도'}
                        </Text>
                    </TouchableOpacity>
                    }
                </View>
            </View>
        );
    }
});

