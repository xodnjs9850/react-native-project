import React, { Component } from 'react';
import {observer} from 'mobx-react';
import {NavigationScreenProp, NavigationState} from 'react-navigation';
import {
    View, 
    Text, 
    TouchableOpacity, 
    Image, 
    Animated, 
    Easing,
    NativeModules,
    } from 'react-native';
import styles from '@utils/styles';

export interface Props {
    navigation: NavigationScreenProp<NavigationState>,
}

interface State {
    useSmartParking: boolean,
}

export default observer(class SensorStart extends Component<Props, State> {
    //static navigationOptions = {headerShown: false};


    render() {
        
        return (
            <View style={styles.root}>
                <View style={styles.body}>
                    <Image
                        style={styles.sensorTestImage}
                        source={require('@assets/images/phone_stay.png')}/>
                </View>
                <View>
                    <TouchableOpacity
                        style={styles.button}
                        activeOpacity={0.5}
                        onPress={this.props.navigation.navigate('SensorTestScreenStack', {})}
                        >
                        <Text style={styles.sensorText}>센서 테스트 시작하기</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    }
});




