import React, { Component, useEffect } from 'react';
import {observer} from 'mobx-react';
import {NavigationScreenProp, NavigationState} from 'react-navigation';
import {
    View, 
    Text, 
    TouchableOpacity, 
    Image, 
    } from 'react-native';
import styles from '@utils/styles';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {
    useSmartParking: boolean,
}

export default observer(class SensorStartScreen extends Component<Props, State> {
    static navigationOptions = {headerShown: false};

    constructor(props: Props) {
        super(props);

        this.state = {
            useSmartParking: false,
        };

    }

    render() {

        const onPress = () => {
            this.props.navigation.navigate('SensorTestScreen', {})
        }
        
        return (
            <View style={styles.root}>
                <View style={styles.body}>
                    <Image
                        style={styles.sensorTestImage}
                        source={require('@assets/images/phone_stay.png')}/>
                </View>
                <View>
                    <Text style={[styles.sensorPrompt2, {fontSize: 14, lineHeight: 24}]}>
                        {'주차위치 인식을 위한 스마트폰의 센서를 테스트합니다.\n반드시 스마트폰의 제어센터에서 화면 회전 방향을\n'}
                        <Text style={{fontSize: 16, color: '#fb836f'}}>
                            {' 세로 화면 방향 고정: 켬 '}
                        </Text>
                        {'으로 하시고 테스트를 시작하세요.'}
                    </Text>
                </View>


                <View>
                    <TouchableOpacity
                        style={styles.button}
                        activeOpacity={0.5}
                        onPress={onPress}
                        >
                        <Text style={styles.sensorText}>센서 테스트 시작하기</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    }
});

