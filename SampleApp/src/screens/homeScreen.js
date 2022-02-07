import React, { Component } from 'react';
import {observer} from 'mobx-react';
import {NavigationScreenProp, NavigationState} from 'react-navigation';
import AsyncStorage from '@react-native-community/async-storage';
import { checkBluetoothState, startBeaconService } from '@service/SmartBeaconFunctionService';
import { AppState, Button, NativeModules, Text, View, TouchableOpacity, Alert } from 'react-native';
import styles from '@utils/styles';

const USE_SMART_ONEPASS_SYSTEM_KEY = 'useSmartOnepassSystem';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {

    sensorTestFlag: boolean,
    autoLogin: boolean,
    useSmartParking: boolean,
    onePass: boolean,
    
}

export default observer(class homeScreen extends Component<Props, State> {
    static navigationOptions = {headerShown: false};

    constructor(props: Props) {
        super(props);

        this.state = {
        };
    }

    async componentDidMount() {
        // login 정보 가져오기 
        AsyncStorage.getItem('loginData', (err, result) => {

            const transData = JSON.parse(result);
            if(result != null) {
                this.setState({
                    onePass: transData.onePass,
                    useSmartParking: transData.useSmartParking,
                })
            }
        })
        // 센서 테스트 정보 가져오기
        AsyncStorage.getItem('sensorTestResult', (err, result) => {

            const transData = JSON.parse(result);
            if(result != null) {
                this.setState({
                    sensorTestFlag: transData,
                })
            }
        })

    }

    _startBeaconService = () => {

        startBeaconService(this.state.onePass, this.state.useSmartParking)

    };

    render() {
        
        return (
            <View style={styles.root}>

                <View style={{marginTop: 50}}>
                    <TouchableOpacity
                        style={styles.button}
                        activeOpacity={0.5}
                        onPress={this._startBeaconService}>
                        <Text style={styles.text2}>BackGround Service</Text>
                    </TouchableOpacity>

                </View>

            </View>
            
        );
    }
});
